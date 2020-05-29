package de.otto.edison.mongo.configuration;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCompressor;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class MongoPropertiesTest {

    @Test
    public void shouldReturnPassword() {
        final MongoProperties props = new MongoProperties();
        props.setPassword("somePassword");
        assertThat(props.getPassword(), is("somePassword"));
    }

    @Test
    void shouldConfigureCompressorListWhenClientServerCompressionIsEnabled() {
        //given
        final MongoProperties props = new MongoProperties();

        //when
        props.setClientServerCompressionEnabled(true);
        MongoClientOptions mongoClientOptions = props.toMongoClientOptions(MongoClient.getDefaultCodecRegistry(), Collections.singletonList(MongoCompressor.createZlibCompressor()));

        //then
        assertThat(mongoClientOptions.getCompressorList(), is(Collections.singletonList(MongoCompressor.createZlibCompressor())));
    }

    @Test
    void shouldNotConfigureCompressorListWhenClientServerCompressionIsDisabled() {
        //given
        final MongoProperties props = new MongoProperties();

        //when
        props.setClientServerCompressionEnabled(false);
        MongoClientOptions mongoClientOptions = props.toMongoClientOptions(MongoClient.getDefaultCodecRegistry(), Collections.singletonList(MongoCompressor.createZlibCompressor()));

        //then
        assertThat(mongoClientOptions.getCompressorList(), is(Collections.emptyList()));
    }
}
