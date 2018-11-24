package net.opentsdb.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnicodeUtil {


    private static final Logger LOG = LoggerFactory.getLogger(UnicodeUtil.class);

    public static String decodePatten = "__.+?__" ;

    public static String encodePatten = "[\\u4e00-\\u9fa5]" ;

    /**
     * 将一段字符串，包含有正则的 __.+?__ 进行unicode转码
     * @param src 不为空的字符串
     * @return 如果有需要转码，则返回转码后的字符串，如果没有，则直接返回空
     */
    public static String decodeString(String src){
        Matcher m = Pattern.compile(decodePatten).matcher(src);
        boolean hasFind = false ;
        while (m.find()){
            hasFind = true ;
            String findIt = m.group(0);
            try {
                String decodeStr = UnicodeUtil.decode(findIt.replaceAll("__",""));
                src = src.replace(findIt,decodeStr);
            } catch (Exception e) {
                LOG.warn("{} decode error , ignore it ",findIt);
            }
        }
        if (hasFind){ //发送有中文字符，重写客户端
           return src ;
        }else{
           return null ;
        }
    }

    /**
     * 字符串编码成Unicode编码
     */
    public static String encode(String src) {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < src.length(); i++) {
            c = src.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128)
                str.append(strHex);
            else
                str.append("00" + strHex); // 低位在前面补00
        }
        return str.toString();
    }

    /**
     * Unicode解码成字符串
     * @param src
     * @return
     */
    public static String decode(String src) throws  Exception{
        int t =  src.length() / 4;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = src.substring(i * 4, (i + 1) * 4); // 每6位描述一个字节
            String s1 = s.substring(0, 2) + "00";  // 高位需要补上00再转
            String s2 = s.substring(2);  // 低位直接转
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);  // 将16进制的string转为int
            char[] chars = Character.toChars(n);   // 将int转换为字符
            str.append(new String(chars));
        }
        return str.toString();
    }

    /**
     * 将list字符串符合正则 __.+?__ 进行转码
     * @param suggestions
     * @return
     */
    public static List<String> decodeList(List<String> suggestions) {
        List<String> tempList = new  ArrayList(suggestions.size());
        for (String str : suggestions){
            if(str.matches(decodePatten)){
                try {
                    str = UnicodeUtil.decode(str.replaceAll("__",""));
                } catch (Exception e) {
                    LOG.warn("{} decode error , ignore it ",str);
                }
            }
            tempList.add(str) ;
        }
        return tempList ;
    }

    /**
     * 将hashMap中的value如果包含中文，则按照正则 __.+?__ 进行编码码
     * @param src
     * @return
     */
    public static HashMap<String,String>  encodeHashMap(HashMap<String, String> src) {
        for(Map.Entry<String, String> entry : src.entrySet()){
            src.put(entry.getKey(),UnicodeUtil.encodeByCn(entry.getValue())) ;
        }
        return src ;
    }

    /**
     * 对包含有中文的key进行编码
     * @param src
     * @return
     */
    public static String encodeByCn(String src) {
        Matcher m = Pattern.compile(encodePatten).matcher(src);
        if (m.find()){
            return "__" + UnicodeUtil.encode(src) + "__"  ;
        }
        return src ;
    }


}
