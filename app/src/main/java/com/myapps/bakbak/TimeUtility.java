package com.myapps.bakbak;

import android.util.Log;

public class TimeUtility {

    public static String getCurrTime(int diff,String messageLondonTime){
        int hr=Integer.parseInt(messageLondonTime.substring(0,2));
        int mn=Integer.parseInt(messageLondonTime.substring(3,5));
        for(int i=0;i<diff;i++){
            mn++;
            if(mn==60){
                mn=0;
                hr++;
                if(hr==24){
                    hr=0;
                }
            }
        }
        String hs,ms;
        if(hr==0){
            hs="00";
        }
        else if(hr<10){
            hs="0"+hr;
        }
        else{
            hs=String.valueOf(hr);
        }
        if(mn==0){
            ms="00";
        }
        else if(mn<10){
            ms="0"+mn;
        }
        else{
            ms=String.valueOf(mn);
        }
        return hs+":"+ms;
    }

    public static int getTimeDifference(String currentThisTime,String currentLondonTime){
        int lh=Integer.parseInt(currentLondonTime.substring(0,2));
        int ch=Integer.parseInt(currentThisTime.substring(0,2));

        int lm=Integer.parseInt(currentLondonTime.substring(3,5));
        int cm=Integer.parseInt(currentThisTime.substring(3,5));

//        Log.i("hj",currentThisTime+" "+currentLondonTime);
        if(lh==ch && lm==cm){
            return 0;
        }
        int mins=0;
        if(ch>lh || ch==lh && lm>cm){
            int ich=lh,icm=lm;
            while(true){
                mins++;
                icm++;
                if(icm==60){
                    icm=0;
                    ich++;
                }
                if(ich==ch && icm==cm){
                    break;
                }
            }
        }
        else{
            int ich=ch,icm=cm;
            while(true){
                mins++;
                icm++;
                if(icm==60){
                    icm=0;
                    ich++;
                }
                if(ich==lh && icm==lm){
                    break;
                }
            }
            mins=1440-mins;
        }

        if(mins>=825){
            return -(1440-mins);
        }
        else{
            return mins;
        }

    }
}
