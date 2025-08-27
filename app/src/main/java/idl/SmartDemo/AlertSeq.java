package idl.SmartDemo;

import com.zrdds.infrastructure.ZRSequence;

public class AlertSeq extends ZRSequence<Alert> {

    protected Object[] alloc_element(int length) {
        Alert[] result = new Alert[length];
        for (int i = 0; i < result.length; ++i) {
             result[i] = new Alert();
        }
        return result;
    }

    protected Object copy_from_element(Object dstEle, Object srcEle){
        Alert typedDst = (Alert)dstEle;
        Alert typedSrc = (Alert)srcEle;
        return typedDst.copy(typedSrc);
    }

    public void pull_from_nativeI(long nativeSeq){

    }

    public void push_to_nativeI(long nativeSeq){

    }
}