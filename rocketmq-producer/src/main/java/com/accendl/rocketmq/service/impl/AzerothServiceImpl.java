package com.accendl.rocketmq.service.impl;

import com.accendl.account.dto.UserDTO;
import com.accendl.azeroth.dto.AzAccountDTO;
import com.accendl.rocketmq.service.IAzerothService;
import com.alibaba.cloud.stream.binder.rocketmq.constant.RocketMQConst;
import org.apache.dubbo.config.annotation.DubboService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

@Service
@DubboService(version = "1.0.0", protocol = "${dubbo.protocol.id}",
        registry = "${dubbo.registry.id}", timeout = 30000)
public class AzerothServiceImpl implements IAzerothService {

    private static final Logger logger = LoggerFactory.getLogger(AzerothServiceImpl.class);

    private final StreamBridge streamBridge;

    public AzerothServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public boolean accountCreate(String userName, String password) throws Exception{
        AzAccountDTO azAccountDTO = new AzAccountDTO(userName, password);
        MessageBuilder<AzAccountDTO> builder = MessageBuilder.withPayload(azAccountDTO);
        builder.setHeader("username", azAccountDTO.getUsername())
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .setHeader(RocketMQConst.USER_TRANSACTIONAL_ARGS, "binder")
                .setHeader(RocketMQConst.PROPERTY_MAX_RECONSUME_TIMES, 3);
        Message<AzAccountDTO> msg = builder.build();
        try {
            boolean flag = streamBridge.send("producer-out-0", msg);
            if (flag){
                logger.info("send Msg success:" + msg);
                return true;
            }else{
                logger.info("send Msg fail:" + msg);
                throw new Exception("消息入队失败");
            }
        }catch (Exception e){
            logger.error("send msg fail: "+e.getMessage());
            throw new Exception(e);
        }
    }

}
