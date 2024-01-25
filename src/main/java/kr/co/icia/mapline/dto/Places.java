package kr.co.icia.mapline.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Places {
    private List<Document> documents; // 검색 결과 문서의 리스트
    private Meta meta; // 검색 결과에 대한 메타 정보

    public Meta getMeta() {
        return meta;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        private boolean is_end; // 현재 페이지가 마지막 페이지인지 여부
        private int pageable_count; // total_count 중에 노출가능 문서 수

        private int total_count; // 검색어에 검색된 문서 수

        public int getTotal_count() {
            return total_count;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document {
        private String phone; // 전화번호
        private String place_name; // 장소명, 업체명
        private String x; // X 좌표값 혹은 longitude
        private String y; // Y 좌표값 혹은 latitude

        private String place_url; // 장소 상세페이지 URL

        public String getPlace_url() {
            return place_url;
        }

        public String getPlace_name() {
            return place_name;
        }

        public String getPhone() {
            return phone;
        }

        public String getX() {
            return x;
        }

        public String getY() {
            return y;
        }
    }
}
