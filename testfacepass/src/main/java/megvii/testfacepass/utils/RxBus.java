package megvii.testfacepass.utils;

import rx.Observable;
import rx.subjects.PublishSubject;

public class RxBus {
    //单例创建
   private static volatile RxBus rxBus;
   private final PublishSubject<Object> mRxtBus=PublishSubject.create();
   public static RxBus getRxBus(){
    //加上线程同步锁
      synchronized (RxBus.class){
        if (rxBus==null){
            rxBus=new RxBus();
        }
     }
      return rxBus;
 }

   //重写发送带Tag值得可以进行比对
 public void post(int tag,Object event){
    //用Message进行封装  这是自己封装的内部类
    Message msg = new Message(tag,event);
    mRxtBus.onNext(msg);
 }
   //创建接受时间的方法
   public <T> Observable<T> toEvent(Class<T> eventType){
    return mRxtBus.ofType(eventType);
}
  //自己封装的传送数据的Bean类
  public class Message{
    private int tag;
    private Object event;
    public Message() {
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }

    public Object getEvent() {
        return event;
    }

    public void setEvent(Object event) {
        this.event = event;
    }
    public Message(int tag,Object event){
        this.tag=tag;
        this.event=event;
     }
  }
}