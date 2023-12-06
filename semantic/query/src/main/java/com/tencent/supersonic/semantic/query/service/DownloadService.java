package com.tencent.supersonic.semantic.query.service;

import com.tencent.supersonic.auth.api.authentication.pojo.User;
import com.tencent.supersonic.semantic.api.query.request.BatchDownloadReq;
import com.tencent.supersonic.semantic.api.query.request.DownloadStructReq;
import javax.servlet.http.HttpServletResponse;

public interface DownloadService {

    void downloadByStruct(DownloadStructReq downloadStructReq,
                          User user, HttpServletResponse response) throws Exception;

    void batchDownload(BatchDownloadReq batchDownloadReq, User user, HttpServletResponse response) throws Exception;
}
