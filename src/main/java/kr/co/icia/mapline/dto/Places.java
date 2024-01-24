package kr.co.icia.mapline.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public class Places {
    private List<Document> documents;
    private Meta meta;

    public Meta getMeta() {
        return meta;
    }

    public List<Document> getDocuments() {
        return documents;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta{
        private boolean is_end;
        private int pageable_count;

        private int total_count;

        public int getTotal_count() {
            return total_count;
        }
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Document{
        private String phone;
        private String place_name;
        private String x;
        private String y;

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
