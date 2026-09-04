package com.timofey.analytics_service.config;

import com.timofey.analytics_service.event.LinkClickedEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        DeadLetterPublishingRecoverer recoverer =
                new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> {

                            System.out.println(
                                    "SENDING TO DLT: " + record.topic()
                            );

                            return new TopicPartition(
                                    record.topic() + ".DLT",
                                    record.partition()
                            );
                        }
                );


        DefaultErrorHandler errorHandler =
                new DefaultErrorHandler(
                        recoverer,
                        new FixedBackOff(1000L, 3)
                );


        errorHandler.setRetryListeners((record, ex, attempt) -> {

            System.out.println(
                    "RETRY #" + attempt +
                            " topic=" + record.topic() +
                            " offset=" + record.offset()
            );

        });


        return errorHandler;
    }


    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, LinkClickedEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, LinkClickedEvent> consumerFactory,
            DefaultErrorHandler kafkaErrorHandler
    ) {

        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, LinkClickedEvent>();

        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(kafkaErrorHandler);

        return factory;
    }
}