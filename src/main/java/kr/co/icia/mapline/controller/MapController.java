package kr.co.icia.mapline.controller;

import ch.qos.logback.core.net.ObjectWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.icia.mapline.util.KakaoApiUtil;
import kr.co.icia.mapline.util.KakaoApiUtil.Point;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller //주석에 표기된 번호 순서대로 볼 것
public class MapController {

    /**
     * 자동차 이동 경로 그리기
     *
     * @param fromAddress 출발지 주소정보
     * @param toAddress   목적지 주소정보
     * @param model       html파일에 값을 전달해주는 객체
     * @return html 파일위치
     */
    //*****3번***** :
    @GetMapping("/map/paths") // url : /map/paths
    public String getMapPaths(@RequestParam(required = false) String fromAddress, // //출발지 입력받음
                              @RequestParam(required = false) String toAddress, // //도착지 입력받음
                              Model model) throws IOException, InterruptedException {
        Point fromPoint = null; //초기 화면에서는 fromPoint가 없기 때문에 오류남 그래서 초기화시켜줌
        Point toPoint = null; // 상기한 바와 같음
        if (fromAddress != null && !fromAddress.isEmpty()) { // null이 아니고(초기화면일때 null) 비어있지 않을 시 실행
            fromPoint = KakaoApiUtil.getPointByAddress(fromAddress); //주소값을 x, y값으로 변환
            model.addAttribute("fromPoint", fromPoint); // html로 보냄
        }
        if (toAddress != null && !toAddress.isEmpty()) { //똑
            toPoint = KakaoApiUtil.getPointByAddress(toAddress); // 같
            model.addAttribute("toPoint", toPoint); // 음
        }

        if (fromPoint != null && toPoint != null) { // 초기화면이면 실행 안 한다는 뜻
            List<Point> pointList = KakaoApiUtil.getVehiclePaths(fromPoint, toPoint); //출발지와 도착지의 x, y값을 getVehiclePaths에 넣어서 반환되는 Point로 구성된 List를 저장
            String pointListJson = new ObjectMapper().writer().writeValueAsString(pointList); //html이 읽을 수 있도록 pointList를 json형태로 변환
            model.addAttribute("pointList", pointListJson); //html로 보냄
        }
        return "map/paths";
    }

    /**
     * 주소를 좌표로 변환
     *
     * @param address 주소정보
     * @param model   html파일에 값을 전달해주는 객체
     * @return html 파일위치
     */
    //*****1번****** : 입력받은 주소를 x, y값으로 변환해줌
    @GetMapping("/map/address/point") // url : /map/address/point
    public String getMapAddressPoint(@RequestParam(required = false) String address, Model model) //html의 input태그로 address를 입력받음
            throws IOException, InterruptedException {                                            //(required=true라면 아무것도 입력되지 않은 초기화면에서 오류가 나기 때문 false로 설정)
        if (address != null && !address.isEmpty()) { //address 값을 정확하게 입력받았을 때만 실행
            Point point = KakaoApiUtil.getPointByAddress(address); //kakaoapiutil class에서 정의한 getPointByAddress메서드로 주소 문자열을 입력받아 Point 객체에 저장
            model.addAttribute("point", point); //point 인스턴스를 html에 나타내기 위해 addAttribute
        }
        return "map/address_point";
    }

    /**
     * 출발지와 목적지를 지도상에 표시하기
     *
     * @param fromAddress 출발지 주소정보
     * @param toAddress   목적지 주소정보
     * @param model       html파일에 값을 전달해주는 객체
     * @return html 파일위치
     */
    //*****2번***** : 출발지와 도착지를 각각 x, y값으로 변환 사실상 getMapAddressPoint의 두배와 다를 바 없음
    @GetMapping("/map/marker") // url : /map/marker
    public String getMapMarker(@RequestParam(required = false) String fromAddress, // // 출발지 주소 입력받음
                               @RequestParam(required = false) String toAddress, // // 도착지 주소 입력받음
                               Model model) throws IOException, InterruptedException {
        if (fromAddress != null && !fromAddress.isEmpty()) {
            Point fromPoint = KakaoApiUtil.getPointByAddress(fromAddress);
            model.addAttribute("fromPoint", fromPoint);
        }
        if (toAddress != null && !toAddress.isEmpty()) {
            Point toPoint = KakaoApiUtil.getPointByAddress(toAddress);
            model.addAttribute("toPoint", toPoint);
        }
        return "map/marker";
    }

    /**
     * 키워드로 검색하기
     *
     * @param keyword 검색어
     * @param x       중심좌표 x
     * @param y       중심좌표 y
     * @param model   html파일에 값을 전달해주는 객체
     * @return html 파일위치
     */
    @GetMapping("/map/keyword")
    public String getKeyword(@RequestParam(required = false) String keyword, @RequestParam(required = false) String option,  Model model) throws IOException, InterruptedException { //y좌표를 입력받음
        if (keyword != null && !keyword.isEmpty()) { //keyword, x, y값이 모두 입력되었을 때 실행
            List<KakaoApiUtil.Marker> markerList = KakaoApiUtil.getPointsByKeyword(keyword); //keyword, x, y값을 getPointsByKeyword에 넣어서 반환되는 Pharmacy로 구성된 List를 저장
            //int cnt = 0; //pharmacyList의 크기를 저장할 변수
            assert markerList != null;
            String markerListJson = new ObjectMapper().writer().writeValueAsString(markerList);
            System.out.println("size: " + markerList.size());
//            for (KakaoApiUtil.Pharmacy pharmacy : markerList) { //pharmacyList의 크기만큼 반복
//                cnt++; //pharmacyList의 크기를 저장
//            }
            //System.out.println(cnt); //pharmacyList의 크기를 출력
            List<Point> paths = KakaoApiUtil.getPathsByMarker(markerList, option);
            for (Point point : paths){
                System.out.println("x: " + point.getX());
                System.out.println("y: " + point.getY());
            }
            System.out.println(markerListJson);
            String pathsListJson = new ObjectMapper().writer().writeValueAsString(paths);
            model.addAttribute("markerList", markerListJson); //html로 보냄
            //System.out.println("실행됨"); //실행됐는지 확인
            model.addAttribute("paths", pathsListJson);
        }
        return "map/keyword"; //html 파일위치
    }


}
