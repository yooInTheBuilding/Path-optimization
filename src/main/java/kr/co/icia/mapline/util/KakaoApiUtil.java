package kr.co.icia.mapline.util;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.icia.mapline.APIKEY;
import kr.co.icia.mapline.dto.Places;
import kr.co.icia.mapline.util.KakaoApiUtil.KakaoDirections.Route.Section.Road;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class KakaoApiUtil {

    private static final String REST_API_KEY = APIKEY.apikey;


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
        KakaoDirections kakaoDirections = getKakaoDirections(from, to);
        List<Point> pointList = new ArrayList<Point>(); //화면에 표시할 Point들을 저장할 pointList 생성
        List<Road> roads = kakaoDirections.getRoutes().get(0).getSections().get(0).getRoads(); //kakaoDirectios-route(getRoutes().get(0))-section(getSections().get(0))-roads(getRoads())
        for (Road road : roads) { //enhanced for문으로 roads안의 road들을 각각 탐색하여 vertexes List를 뽑아냄
            List<Double> vertexes = road.getVertexes();
            for (int i = 0; i < vertexes.size(); i++) {
                pointList.add(new Point(vertexes.get(i), vertexes.get(++i))); // 뽑아낸 vertexes List의 vertex의 x, y값을 pointList에 저장
            }
        }
        return pointList; //pointList 반환

    }

    public static KakaoDirections getKakaoDirections(Point from, Point to) throws IOException, InterruptedException {
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
        //System.out.println(responseBody);
        //저어기 밑에 정의된 KakaoDirectios class 보고오기
        return new ObjectMapper().readValue(responseBody, KakaoDirections.class);//responseBody를 KakaoDirections 인스턴스에 저장
    }

    public static int getDistance(Point from, Point to) throws IOException, InterruptedException {//요약: 출발지와 도착지의 x, y값을 입력받아 거리를 구한 그 거리를 반환
        KakaoDirections kakaoDirections = getKakaoDirections(from, to);//출발지와 도착지의 x, y값을 입력받아 경로를 구한 그 경로를 point의 List로 반환(선은 점들의 집합임)
        if (kakaoDirections == null || kakaoDirections.getRoutes() == null) {
            throw new IllegalArgumentException("없음");
        }
        return kakaoDirections.getRoutes().get(0).getSummary().getDistance();//kakaoDirections의 route(getRoutes().get(0))-summary(getSummary())-distance(getDistance())
    }

    public static int getDuration(Point from, Point to) throws IOException, InterruptedException { //요약: 출발지와 도착지의 x, y값을 입력받아 시간을 구한 그 시간을 반환
        KakaoDirections kakaoDirections = getKakaoDirections(from, to); //출발지와 도착지의 x, y값을 입력받아 경로를 구한 그 경로를 point의 List로 반환(선은 점들의 집합임)
        if (kakaoDirections == null || kakaoDirections.getRoutes() == null) {
            throw new IllegalArgumentException("없음");
        }
        return kakaoDirections.getRoutes().get(0).getSummary().getDuration(); //kakaoDirections의 route(getRoutes().get(0))-summary(getSummary())-duration(getDuration())
    }

    public static List<Marker> getShortestPath(List<Marker> markers, Point pointOrigin) throws IOException, InterruptedException { //요약: markers, x, y값을 입력받아 Pharmacy의 List를 반환하는 함수 getShortestPath
        List<Marker> shortestPath = new ArrayList<>(); //화면에 표시할 Marker들을 저장할 shortestPath 생성
        Marker current = null; //현재 위치를 저장할 current 생성

        for (Marker marker : markers) { //enhanced for문으로 markers안의 marker들을 각각 탐색하여 current에 저장
            if (marker.getX().equals(pointOrigin.getX()) && marker.getY().equals(pointOrigin.getY())) { //marker의 x, y값이 pointOrigin의 x, y값과 같을 경우
                current = marker; //current에 marker를 저장
                break;  //반복문 종료
            }
        }

        if (current == null) { //current가 null일 경우
            throw new IllegalArgumentException("없음"); //예외처리
        }

        shortestPath.add(current); //shortestPath에 current를 추가
        markers.remove(current); //markers에서 current를 제거

        while (!markers.isEmpty()) { //markers가 비어있지 않을 경우
            Marker closest = null; //가장 가까운 marker를 저장할 closest 생성
            int shortestDistance = Integer.MAX_VALUE; //가장 가까운 marker와의 거리를 저장할 shortestDistance 생성

            for (Marker marker : markers) { //enhanced for문으로 markers안의 marker들을 각각 탐색하여 shortestDistance와 closest를 구함
                int distance = getDistance(new Point(current.getX(), current.getY()), new Point(marker.getX(), marker.getY())); //current와 marker의 거리를 구함
                if (distance < shortestDistance) { //distance가 shortestDistance보다 작을 경우
                    closest = marker; //closest에 marker를 저장
                    shortestDistance = distance; //shortestDistance에 distance를 저장
                }
            }

            shortestPath.add(closest); //shortestPath에 closest를 추가
            markers.remove(closest); //markers에서 closest를 제거
            current = closest; //current에 closest를 저장
        }


        shortestPath.add(shortestPath.get(0)); //shortestPath의 첫번째 값을 마지막에 추가

        return shortestPath; //shortestPath 반환
    }

    public static List<Marker> getShortestPathByMultiThread(List<Marker> markers, Point pointOrigin) throws IOException, InterruptedException, ExecutionException {
        List<Marker> shortestPath = new ArrayList<>();
        Marker current = null;

        for (Marker marker : markers) {
            if (marker.getX().equals(pointOrigin.getX()) && marker.getY().equals(pointOrigin.getY())) {
                current = marker;
                break;
            }
        }

        if (current == null) {
            throw new IllegalArgumentException("없음");
        }

        shortestPath.add(current);
        markers.remove(current);

        ExecutorService executor = Executors.newFixedThreadPool(5); // 병렬 처리를 위한 스레드 풀 생성

        while (!markers.isEmpty()) {
            Marker closest = null;
            int shortestDistance = Integer.MAX_VALUE;

            List<CompletableFuture<Integer>> futures = new ArrayList<>(); // 각 마커에 대한 거리 계산 결과를 저장할 Future 리스트

            for (Marker marker : markers) {
                Marker finalCurrent = current;
                CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        assert finalCurrent != null;
                        System.out.println("current: " + finalCurrent.getX() + ", " + finalCurrent.getY());
                        System.out.println("marker: " + marker.getX() + ", " + marker.getY());
                        return getDistance(new Point(finalCurrent.getX(), finalCurrent.getY()), new Point(marker.getX(), marker.getY()));
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, executor); // 각 마커에 대한 거리 계산을 비동기적으로 수행

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // 모든 거리 계산이 완료될 때까지 기다림

            for (int i = 0; i < markers.size(); i++) {
                int distance = futures.get(i).get(); // 각 마커에 대한 거리 계산 결과를 가져옴
                if (distance < shortestDistance) {
                    closest = markers.get(i);
                    shortestDistance = distance;
                }
            }

            shortestPath.add(closest);
            markers.remove(closest);
            current = closest;
        }

        executor.shutdown(); // 스레드 풀 종료

        shortestPath.add(shortestPath.get(0));

        return shortestPath;
    }
    public static List<Marker> getShortestPathByMultiThread2(List<Marker> markers, Point pointOrigin) throws IOException, InterruptedException, ExecutionException {
        List<Marker> shortestPath = new ArrayList<>();
        Marker current = null;

        for (Marker marker : markers) {
            if (marker.getX().equals(pointOrigin.getX()) && marker.getY().equals(pointOrigin.getY())) {
                current = marker;
                break;
            }
        }

        if (current == null) {
            throw new IllegalArgumentException("없음");
        }

        shortestPath.add(current);
        markers.remove(current);

        ExecutorService executor = Executors.newFixedThreadPool(10); // 병렬 처리를 위한 스레드 풀 생성

        while (!markers.isEmpty()) {
            Marker closest = null;
            Double shortestDistance = (double) Integer.MAX_VALUE;

            List<CompletableFuture<Double>> futures = new ArrayList<>(); // 각 마커에 대한 거리 계산 결과를 저장할 Future 리스트

            for (Marker marker : markers) {
                Marker finalCurrent = current;
                CompletableFuture<Double> future = CompletableFuture.supplyAsync(() -> {
                    assert finalCurrent != null;
                    System.out.println("current: " + finalCurrent.getX() + ", " + finalCurrent.getY());
                    System.out.println("marker: " + marker.getX() + ", " + marker.getY());
                    //return getDistance(new Point(finalCurrent.getX(), finalCurrent.getY()), new Point(marker.getX(), marker.getY()));
                    return (marker.getX() - finalCurrent.getX()) * (marker.getX() - finalCurrent.getX()) + (marker.getY() - finalCurrent.getY()) * (marker.getY() - finalCurrent.getY());
                }, executor); // 각 마커에 대한 거리 계산을 비동기적으로 수행

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // 모든 거리 계산이 완료될 때까지 기다림

            for (int i = 0; i < markers.size(); i++) {
                Double distance = futures.get(i).get(); // 각 마커에 대한 거리 계산 결과를 가져옴
                if (distance < shortestDistance) {
                    closest = markers.get(i);
                    shortestDistance = distance;
                }
            }

            shortestPath.add(closest);
            markers.remove(closest);
            current = closest;
        }

        executor.shutdown(); // 스레드 풀 종료

        shortestPath.add(shortestPath.get(0));

        return shortestPath;
    }

    public static List<Marker> getShortestTime(List<Marker> markers, Point pointOrigin) throws IOException, InterruptedException {
        List<Marker> shortestTime = new ArrayList<>();
        Marker current = null;

        for (Marker marker : markers) {
            if (marker.getX().equals(pointOrigin.getX()) && marker.getY().equals(pointOrigin.getY())) {
                current = marker;
                break;
            }
        }

        if (current == null) {
            throw new IllegalArgumentException("없음");
        }

        shortestTime.add(current);
        markers.remove(current);

        while (!markers.isEmpty()) {
            Marker closest = null;
            int shortestDuration = Integer.MAX_VALUE;

            for (Marker marker : markers) {
                int duration = getDuration(new Point(current.getX(), current.getY()), new Point(marker.getX(), marker.getY()));
                if (duration < shortestDuration) {
                    closest = marker;
                    shortestDuration = duration;
                }
            }

            shortestTime.add(closest);
            markers.remove(closest);
            current = closest;
        }

        // Add pointOrigin as the final destination
        shortestTime.add(shortestTime.get(0));
        return shortestTime;
    }

    public static List<Marker> getShortestTimeByMultiThread(List<Marker> markers, Point pointOrigin) throws IOException, InterruptedException, ExecutionException {
        List<Marker> shortestPath = new ArrayList<>();
        Marker current = null;

        for (Marker marker : markers) {
            if (marker.getX().equals(pointOrigin.getX()) && marker.getY().equals(pointOrigin.getY())) {
                current = marker;
                break;
            }
        }

        if (current == null) {
            throw new IllegalArgumentException("없음");
        }

        shortestPath.add(current);
        markers.remove(current);

        ExecutorService executor = Executors.newFixedThreadPool(5); // 병렬 처리를 위한 스레드 풀 생성

        while (!markers.isEmpty()) {
            Marker closest = null;
            int shortestDuration = Integer.MAX_VALUE;

            List<CompletableFuture<Integer>> futures = new ArrayList<>(); // 각 마커에 대한 거리 계산 결과를 저장할 Future 리스트

            for (Marker marker : markers) {
                Marker finalCurrent = current;
                CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
                    try {
                        assert finalCurrent != null;
                        System.out.println("current: " + finalCurrent.getX() + ", " + finalCurrent.getY());
                        System.out.println("marker: " + marker.getX() + ", " + marker.getY());
                        return getDuration(new Point(finalCurrent.getX(), finalCurrent.getY()), new Point(marker.getX(), marker.getY()));
                    } catch (IOException | InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }, executor); // 각 마커에 대한 거리 계산을 비동기적으로 수행

                futures.add(future);
            }

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // 모든 거리 계산이 완료될 때까지 기다림

            for (int i = 0; i < markers.size(); i++) {
                int duration = futures.get(i).get(); // 각 마커에 대한 거리 계산 결과를 가져옴
                if (duration < shortestDuration) {
                    closest = markers.get(i);
                    shortestDuration = duration;
                }
            }

            shortestPath.add(closest);
            markers.remove(closest);
            current = closest;
        }

        executor.shutdown(); // 스레드 풀 종료

        shortestPath.add(shortestPath.get(0));

        return shortestPath;
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
        //System.out.println("Point: " + responseBody);//출력

        //밑의 줄을 보기 전에 밑에 정의된 KakaoAddress 클래스 참조
        KakaoAddress kakaoAddress = new ObjectMapper().readValue(responseBody, KakaoAddress.class);//responseBody를 kakaoAdress 인스턴스에 저장함
        List<KakaoAddress.Document> documents = kakaoAddress.getDocuments(); //응답받은 값(kakaoaddress)을 documents(document로 이루어진 List)에 저장
        if (documents.isEmpty()) { // 값이 없을 경우 오류 가능성이 있어 null처리
            return null;
        }
        KakaoAddress.Document document = documents.get(0); //요청한 좌표값은 어차피 하나이므로 documents리스트의 첫째 값을 받아 document 인스턴스에 저장
        return new Point(document.getX(), document.getY()); //document의 x, y값을 Point 객체에 담아 반환
    }

    /**
     * 키워드로 장소 검색
     *
     * @param keyword 검색어
     * @return 장소 목록
     */
    public static List<Marker> getPointsByKeyword(String keyword) throws IOException, InterruptedException { //요약: keyword, x, y값을 입력받아 Pharmacy의 List를 반환하는 함수 getPointsByKeyword
        Point academy = getPointByAddress("인천광역시 미추홀구 매소홀로488번길 6-32 태승빌딩 5층");
        HttpClient client = HttpClient.newHttpClient(); //api 요청을 보낼 수 있게 하는 http client 객체
        String url = "https://dapi.kakao.com/v2/local/search/keyword.json"; //api 주소
        assert academy != null;
        url += "?query=" + URLEncoder.encode(keyword, "UTF-8") //api 주소에 인코딩 방식 추가
                + "&x=" + academy.getX() //api 주소에 x값 추가
                + "&y=" + academy.getY() //api 주소에 y값 추가
                + "&radius=5000"
                + "&sort=distance";
        HttpRequest request = HttpRequest.newBuilder() //api 요청 양식
                .header("Authorization", "KakaoAK " + REST_API_KEY) //api 요청 헤더(api 인증키 포함)
                .uri(URI.create(url)) //api 요청 주소
                .GET() //GET방식으로 가져옴
                .build(); //api 요청 양식 완성
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString()); //요청을 보내서 온 응답을 response에 저장
        String responseBody = response.body(); //응답받은 값의 body값을 responseBody에 저장
        //System.out.println("keyword: " + responseBody); //출력

        List<Marker> markerList = new ArrayList<>(); //화면에 표시할 Pharmacy들을 저장할 markerList 생성 저어기 밑에 pharmacy class 보고오기

        Places places = new ObjectMapper().readValue(responseBody, Places.class); //응답받은 값(responseBody)을 Places 인스턴스에 저장
        List<Places.Document> documents = places.getDocuments(); //응답받은 값(places)을 documents(document로 이루어진 List)에 저장
        Places.Meta meta = places.getMeta(); //응답받은 값(places)을 meta에 저장
        if (documents.isEmpty() || meta == null) { // 값이 없을 경우 오류 가능성이 있어 null처리
            return null; //null 반환
        }
        int cnt = 0;
        for (Places.Document document : documents) { //enhanced for문으로 documents안의 document들을 각각 탐색하여 Pharmacy 객체를 생성하고 pharmacyList에 저장
            Marker marker = new Marker(Double.parseDouble(document.getX()), //document의 x, y값을 Pharmacy 객체에 담아 pharmacyList에 저장
                    Double.parseDouble(document.getY()), document.getPlace_name(), document.getPhone(), cnt, document.getPlace_url()); //document의 place_name, phone값을 Pharmacy 객체에 담아 pharmacyList에 저장

            markerList.add(marker);

            cnt++;
        }
        if (meta.getTotal_count() > 15) { //검색된 장소가 15개를 넘어갈 경우
            int temp = 0;
            if (meta.getTotal_count() / 15 < 2) {
                temp = meta.getTotal_count() / 15;
            } else {
                temp = 2;
            }
            for (int i = 1; i <= temp; i++) { //15개씩 나눠서 검색
                String url2 = url + "&page=" + (i + 1); //api 주소에 page값 추가
                //System.out.println(url2); //출력
                HttpRequest request2 = HttpRequest.newBuilder() //api 요청 양식
                        .header("Authorization", "KakaoAK " + REST_API_KEY) //api 요청 헤더(api 인증키 포함)
                        .uri(URI.create(url2))//api 요청 주소
                        .GET()//GET방식으로 가져옴
                        .build();//api 요청 양식 완성
                HttpResponse<String> response2 = client.send(request2, HttpResponse.BodyHandlers.ofString());//요청을 보내서 온 응답을 response에 저장
                String responseBody2 = response2.body();//응답받은 값의 body값을 responseBody에 저장
                //System.out.println("keyword: " + responseBody2);//출력
                Places places2 = new ObjectMapper().readValue(responseBody2, Places.class);//응답받은 값(responseBody2)을 Places 인스턴스에 저장
                List<Places.Document> documents2 = places2.getDocuments();//응답받은 값(places2)을 documents2(document로 이루어진 List)에 저장
                Places.Meta meta2 = places2.getMeta();//응답받은 값(places2)을 meta2에 저장
                if (documents2.isEmpty() || meta2 == null) {// 값이 없을 경우 오류 가능성이 있어 null처리
                    return null;//null 반환
                }
                for (Places.Document document : documents2) {//enhanced for문으로 documents2안의 document들을 각각 탐색하여 Pharmacy 객체를 생성하고 pharmacyList에 저장
                    Marker marker2 = new Marker(Double.parseDouble(document.getX()),//document의 x, y값을 Pharmacy 객체에 담아 pharmacyList에 저장
                            Double.parseDouble(document.getY()), document.getPlace_name(), document.getPhone(), cnt, document.getPlace_url());//document의 place_name, phone값을 Pharmacy 객체에 담아 pharmacyList에 저장

                    markerList.add(marker2);

                    cnt++;
                }
            }
        }
        return markerList;//markerList 반환
    }

    public static List<Point> getPathsByMarker(List<Marker> markerList, String option) throws IOException, InterruptedException, ExecutionException {
        Point pointOrigin = getPointByAddress("인천광역시 미추홀구 매소홀로488번길 6-32 태승빌딩 5층");
        assert pointOrigin != null;
        Marker markerOrigin = new Marker(pointOrigin.getX(), pointOrigin.getY(), "인천일보아카데미", "0101", 1, "url");
        markerList.add(markerOrigin);
        List<Marker> shortestMarkers = null;
        if (option.equals("distance")) {
            shortestMarkers = getShortestPathByMultiThread(markerList, pointOrigin);
            System.out.println(option);
        } else if (option.equals("duration")) {
            shortestMarkers = getShortestTimeByMultiThread(markerList, pointOrigin);
            System.out.println(option);
        }
        List<Point> pointList = new ArrayList<>();
        List<Point> paths = new ArrayList<>();
        assert shortestMarkers != null;
        for (Marker marker : shortestMarkers) {
            Point point = new Point(marker.getX(), marker.getY());
            pointList.add(point);
        }
        for (int i = 0; i < pointList.size() - 1; i++) {
            paths.addAll(getVehiclePaths(pointList.get(i), pointList.get(i + 1)));
        }
        return paths;
    }

    @JsonIgnoreProperties(ignoreUnknown = true) //api로 받아온 json값에서 원하는 값(vertexes)만 추출하기 위해 class안에 class가 들어가는 식으로 분리함
    public static class KakaoDirections {       // KakaoDiretions 안에 Route 요소 안에 Section 요소 안에 Road요소 안에 vertexes가 들어있는 구조 (마트료시카?)
        private List<Route> routes;

        public List<Route> getRoutes() {
            return routes;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Route {

            private Summary summary;
            private List<Section> sections;

            public Summary getSummary() {
                return summary;
            }

            public List<Section> getSections() {
                return sections;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Summary {
                private int distance;
                private int duration;

                public int getDistance() {
                    return distance;
                }

                public int getDuration() {
                    return duration;
                }
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


    public static class Marker { //Pharmacy class는 x, y, name, tel로 이루어져 있음
        private Double x; //x값
        private Double y; //y값
        private String name; //이름

        private String tel; //전화번호

        public Marker(Double x, Double y, String name, String tel, int id, String url) {
            this.x = x;
            this.y = y;
            this.name = name;
            this.tel = tel;
            this.id = id;
            this.url = url;
        }

        private int id;

        private String url; //url

        public int getId() {
            return id;
        }

        public Double getX() {
            return x;
        }

        public Double getY() {
            return y;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public String getTel() {
            return tel;
        }

    }
}
