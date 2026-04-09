package in.handyman.raven.lib;

public interface IRadonCommonAction {
    int getConnectTimeOut();

    int getWriteTimeOut();

    int getReadTimeout();

    int getCallTimeout();

    String getHttpClientType();
}
