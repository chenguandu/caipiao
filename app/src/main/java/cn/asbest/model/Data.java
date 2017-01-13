package cn.asbest.model;

public class Data {

    private String expect;
    private String opencode;
    private String opentime;
    private long opentimestamp;
    public void setExpect(String expect) {
         this.expect = expect;
     }
     public String getExpect() {
         return expect;
     }

    public void setOpencode(String opencode) {
         this.opencode = opencode;
     }
     public String getOpencode() {
         return opencode;
     }

    public void setOpentime(String opentime) {
         this.opentime = opentime;
     }
     public String getOpentime() {
         return opentime;
     }

    public void setOpentimestamp(long opentimestamp) {
         this.opentimestamp = opentimestamp;
     }
     public long getOpentimestamp() {
         return opentimestamp;
     }

}