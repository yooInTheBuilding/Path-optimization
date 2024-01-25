package kr.co.icia.mapline.util;

import kr.co.icia.mapline.util.KakaoApiUtil.Point;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

public class KakaoUtilTest {

    @Test
    public void getPointByAddressTest() throws IOException, InterruptedException {
        Point point = KakaoApiUtil.getPointByAddress("인천광역시 미추홀구 매소홀로488번길 6-32 태승빌딩 5층");
        System.out.println("x:" + point.getX() + ",y:" + point.getY());
    }

    @Test
    public void getVehiclePathsTest() throws IOException, InterruptedException {
        Point from = KakaoApiUtil.getPointByAddress("인천광역시 미추홀구 매소홀로488번길 6-32 태승빌딩 5층");
        System.out.println("인천일보아카데미) x:" + from.getX() + ",y:" + from.getY());
        Point to = KakaoApiUtil.getPointByAddress("와우산로 23길 20 패스트파이브 5층");
        System.out.println("브이유에스) x:" + to.getX() + ",y:" + to.getY());

        System.out.println("출발!!");
        List<Point> vehiclePaths = KakaoApiUtil.getVehiclePaths(from, to);
        for (Point point : vehiclePaths) {
            System.out.println("x:" + point.getX() + ",y:" + point.getY());
        }
    }

    @Test
    public void getKeywordTest() throws IOException, InterruptedException {
        List<KakaoApiUtil.Pharmacy> pharmacyList = KakaoApiUtil.getPointsByKeyword("약국", "127.105432", "37.359596");
        StringBuilder sb = new StringBuilder();
        assert pharmacyList != null;
        for (KakaoApiUtil.Pharmacy pharmacy : pharmacyList) {
            sb.append("x: ").append(pharmacy.getX()).append("\n");
            sb.append("y: ").append(pharmacy.getY()).append("\n");
            sb.append("name: ").append(pharmacy.getName()).append("\n");
            sb.append("tel: ").append(pharmacy.getTel()).append("\n");
        }
        System.out.println(sb);
    }
}
