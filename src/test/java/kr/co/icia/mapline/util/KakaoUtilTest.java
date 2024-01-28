package kr.co.icia.mapline.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.co.icia.mapline.util.KakaoApiUtil.Point;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KakaoUtilTest {

    @Test
    public void getPointByAddressTest() throws IOException, InterruptedException {
        Point point = KakaoApiUtil.getPointByAddress("인천광역시 미추홀구 매소홀로488번길 6-32 태승빌딩 5층");
        System.out.println("x:" + point.getX() + ",y:" + point.getY());
    }

    @Test
    public void getVehiclePathsTest() throws IOException, InterruptedException {
        Point from = KakaoApiUtil.getPointByAddress("인천광역시 미추홀구 매소홀로488번길 6-32 태승빌딩 5층");
        assert from != null;
        System.out.println("인천일보아카데미) x:" + from.getX() + ",y:" + from.getY());
        Point to = KakaoApiUtil.getPointByAddress("와우산로 23길 20 패스트파이브 5층");
        assert to != null;
        System.out.println("브이유에스) x:" + to.getX() + ",y:" + to.getY());

        System.out.println("출발!!");
        List<Point> vehiclePaths = KakaoApiUtil.getVehiclePaths(from, to);
        for (Point point : vehiclePaths) {
            System.out.println("x:" + point.getX() + ",y:" + point.getY());
        }
    }
    
    @Test
    public void getDistanceTest() throws IOException, InterruptedException {
        Point from = KakaoApiUtil.getPointByAddress("인천광역시 미추홀구 매소홀로488번길 6-32 태승빌딩 5층");
        assert from != null;
        System.out.println("인천일보아카데미) x:" + from.getX() + ",y:" + from.getY());
        Point to = KakaoApiUtil.getPointByAddress("와우산로 23길 20 패스트파이브 5층");
        assert to != null;
        System.out.println("브이유에스) x:" + to.getX() + ",y:" + to.getY());
        int distance = KakaoApiUtil.getDistance(from, to);
        System.out.println("distance = " + distance);
    }
    
    @Test
    public void getDurationTest() throws IOException, InterruptedException {
        Point from = KakaoApiUtil.getPointByAddress("인천광역시 미추홀구 매소홀로488번길 6-32 태승빌딩 5층");
        assert from != null;
        System.out.println("인천일보아카데미) x:" + from.getX() + ",y:" + from.getY());
        Point to = KakaoApiUtil.getPointByAddress("와우산로 23길 20 패스트파이브 5층");
        assert to != null;
        System.out.println("브이유에스) x:" + to.getX() + ",y:" + to.getY());
        int duration = KakaoApiUtil.getDuration(from, to);
        System.out.println("duration = " + duration);
    }

    @Test
    public void getKeywordTest() throws IOException, InterruptedException {
        List<KakaoApiUtil.Marker> markerList = KakaoApiUtil.getPointsByKeyword("약국");
        StringBuilder sb = new StringBuilder();
        assert markerList != null;
        for (KakaoApiUtil.Marker marker : markerList) {
            sb.append("x: ").append(marker.getX()).append("\n");
            sb.append("y: ").append(marker.getY()).append("\n");
            sb.append("name: ").append(marker.getName()).append("\n");
            sb.append("tel: ").append(marker.getTel()).append("\n");
        }
        System.out.println(sb);
    }
    @Test
    public void getPathsByMarkerTest() throws IOException, InterruptedException {
        List<Point> paths = KakaoApiUtil.getPathsByMarker(Objects.requireNonNull(KakaoApiUtil.getPointsByKeyword("약국")));
        StringBuilder sb = new StringBuilder();
        int cnt = 0;
        for (Point point : paths){
            sb.append("cnt: ").append(cnt).append("\n");
            sb.append("x: ").append(point.getX()).append("\n");
            sb.append("y: ").append(point.getY()).append("\n");
            cnt++;
        }
        System.out.println(sb);
    }

    @Test
    public void objectWriterTest() throws IOException, InterruptedException {
        List<KakaoApiUtil.Marker> markerList = KakaoApiUtil.getPointsByKeyword("약국");
        String markerListJson = new ObjectMapper().writer().writeValueAsString(markerList);
        System.out.println(markerListJson);
    }
}
