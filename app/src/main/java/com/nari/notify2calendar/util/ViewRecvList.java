package com.nari.notify2calendar.util;

public class ViewRecvList {

    String _id ;
    String inPhoneNumber  ;
    String strBody ;
    String chkValue ;
    String regDate ;
    String eventID ;

    public void setId(String _id) {
        this._id = _id ;
    }
    public void setInPhoneNumber(String inPhoneNumber) {
        this.inPhoneNumber = inPhoneNumber ;
    }
    public void setStrBody(String strBody) {
        this.strBody = strBody ;
    }
    public void setChkValue(String chkValue) {
        this.chkValue = chkValue ;
    }
    public void setRegDate(String regDate) {
        this.regDate = regDate.replaceAll("-", "") ;
    }
    public void setEventId(String eventID) {
        this.eventID = eventID ;
    }

    public String getId() {
        return this._id ;
    }
    public String getInPhoneNumber() {
        return this.inPhoneNumber ;
    }
    public String getStrBody() {
        return this.strBody ;
    }
    public String getChkValue() {
        return this.chkValue ;
    }
    public String getRegDate() {
        String rValue = this.regDate.substring(0, 4) + "-" + this.regDate.substring(4, 6) + "-" + this.regDate.substring(6, 8) ;
        return rValue ;
    }
    public String getEventID() {
        return this.eventID ;
    }
}
