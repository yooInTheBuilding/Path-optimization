package kr.co.icia.mapline.util;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.co.icia.mapline.util.KakaoApiUtil.KakaoDirections.Route.Section.Road;

public class KakaoApiUtil {
    private static final String REST_API_KEY = "82684ed9dc3e4e27a58d6d9389868e88";

    /**
     * 자동차 길찾기
     *
     * @param from 출발지
     * @param to   도착지
     * @return 이동 좌표 목록
     * @throws InterruptedException
     * @throws IOException
     */
    public static List<Point> getVehiclePaths(Point from, Point to) throws IOException, InterruptedException {//요약: 출발지와 도착지의 x, y값을 입력받아 경로를 구한 그 경로를 point의 List로 반환(선은 점들의 집합임)
        HttpClient client = HttpClient.newHttpClient(); //api 요청을 할 수 있는 client객체 생성
        String url = "https://apis-navi.kakaomobility.com/v1/directions"; //길찾기 api 주소
        url += "?origin=" + from.getX() + "," + from.getY(); //요청할 api의 출발지 값 설정
        url += "&destination=" + to.getX() + "," + to.getY(); // 요청할 api의 도착지 값 설정
        HttpRequest request = HttpRequest.newBuilder()// //api 요청 형식 작성
                .header("Authorization", "KakaoAK " + REST_API_KEY)//
                .header("Content-Type", "application/json")//
                .uri(URI.create(url))//
                .GET()//
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); //api 요청을 보내고 반환된 값을 response 인스턴스에 저장
        String responseBody = response.body(); //response 인스턴스의 바디값은 responseBody에 저장
        List<Point> pointList = new ArrayList<Point>(); //화면에 표시할 Point들을 저장할 pointList 생성
        //저어기 밑에 정의된 KakaoDirectios class 보고오기
        KakaoDirections kakaoDirections = new ObjectMapper().readValue(responseBody, KakaoDirections.class); //응답값을 kakaoDirections에 저장
        List<Road> roads = kakaoDirections.getRoutes().get(0).getSections().get(0).getRoads(); //kakaoDirectios-route(getRoutes().get(0))-section(getSections().get(0))-roads(getRoads())
        for (Road road : roads) { //enhanced for문으로 roads안의 road들을 각각 탐색하여 vertexes List를 뽑아냄
            List<Double> vertexes = road.getVertexes();
            for (int i = 0; i < vertexes.size(); i++) {
                pointList.add(new Point(vertexes.get(i), vertexes.get(++i))); // 뽑아낸 vertexes List의 vertex의 x, y값을 pointList에 저장
            }
        }

        return pointList; //pointList 반환

    }

    /**
     * 주소 -> 좌표 변환
     *
     * @param address 주소
     * @return 좌표
     */
    public static Point getPointByAddress(String address) throws IOException, InterruptedException {//요약: 주소 문자열을 입력받아 x, y값을 담고 있는 Point 객체를 반환하는 함수 getPointByAddress
        HttpClient client = HttpClient.newHttpClient(); //api 요청을 보낼 수 있게 하는 http client 객체
        String url = "https://dapi.kakao.com/v2/local/search/address.json"; //api 주소
        url += "?query=" + URLEncoder.encode(address, "UTF-8"); //api 주소에 인코딩 방식 추가
        HttpRequest request = HttpRequest.newBuilder()// //api 요청 양식
                .header("Authorization", "KakaoAK " + REST_API_KEY)// //api 요청 헤더(api 인증키 포함)
                .uri(URI.create(url))//
                .GET()// //GET방식으로 가져옴
                .build();
        //요청을 보내서 온 응답을 response에 저장   //client인스턴스의 send메서드에 request(api요청 양식)을 담아서 api 요청
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String responseBody = response.body();//응답받은 값의 body값을 responseBody에 저장
        System.out.println(responseBody);//출력

        //밑의 줄을 보기 전에 밑에 정의된 KakaoAddress 클래스 참조
        KakaoAddress kakaoAddress = new ObjectMapper().readValue(responseBody, KakaoAddress.class);//responseBody를 kakaoAdress 인스턴스에 저장함
        List<KakaoAddress.Document> documents = kakaoAddress.getDocuments(); //응답받은 값(kakaoaddress)을 documents(document로 이루어진 List)에 저장
        if (documents.isEmpty()) { // 값이 없을 경우 오류 가능성이 있어 null처리
            return null;
        }
        KakaoAddress.Document document = documents.get(0); //요청한 좌표값은 어차피 하나이므로 documents리스트의 첫째 값을 받아 document 인스턴스에 저장
        return new Point(document.getX(), document.getY()); //document의 x, y값을 Point 객체에 담아 반환
    }

    @JsonIgnoreProperties(ignoreUnknown = true) //api로 받아온 json값에서 원하는 값(vertexes)만 추출하기 위해 class안에 class가 들어가는 식으로 분리함
    public static class KakaoDirections {       // KakaoDiretions 안에 Route 요소 안에 Section 요소 안에 Road요소 안에 vertexes가 들어있는 구조 (마트료시카?)
        private List<Route> routes;

        public List<Route> getRoutes() {
            return routes;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Route {
            private List<Section> sections;

            public List<Section> getSections() {
                return sections;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Section {
                private List<Road> roads;

                public List<Road> getRoads() {
                    return roads;
                }

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Road {
                    private List<Double> vertexes;

                    public List<Double> getVertexes() {
                        return vertexes;
                    }

                }

            }
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class KakaoAddress {
        private List<Document> documents; //(x, y로 이루어진 Document)의 List

        public List<Document> getDocuments() {
            return documents;
        } //getter

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Document {
            private Double x;
            private Double y;

            public Double getX() {
                return x;
            }

            public Double getY() {
                return y;
            }
        }
    }

    public static class Point {
        private Double x;
        private Double y;

        public Point(Double x, Double y) {
            this.x = x;
            this.y = y;
        }

        public Double getX() {
            return x;
        }

        public Double getY() {
            return y;
        }

    }
}
