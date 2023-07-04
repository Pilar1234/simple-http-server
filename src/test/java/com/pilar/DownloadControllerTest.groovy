package com.pilar

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.web.WebAppConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.context.WebApplicationContext
import spock.lang.Specification

import java.time.Instant

import static com.google.common.net.HttpHeaders.*
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebAppConfiguration
@ContextConfiguration(classes = [MainApplication])
@ActiveProfiles("test")
class DownloadControllerTest extends Specification {

    private MockMvc mockMvc


    @Autowired
    void setWebApplicationContext(WebApplicationContext wac) {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build()
    }

    def 'should send file if ETag not present'() {
        expect:
        mockMvc
                .perform(
                        get('/download/' + FileExamples.TXT_FILE_UUID))
                .andExpect(
                        status().isOk())
    }

    def 'should send file if ETag present but not matching'() {
        expect:
        mockMvc
                .perform(
                        get('/download/' + FileExamples.TXT_FILE_UUID)
                                .header(IF_NONE_MATCH, '"WHATEVER"'))
                .andExpect(
                        status().isOk())
    }

    def 'should not send file if ETag matches content'() {
        given:
        String etag = FileExamples.TXT_FILE.getEtag()
        expect:
        mockMvc
                .perform(
                        get('/download/' + FileExamples.TXT_FILE_UUID)
                                .header(IF_NONE_MATCH, etag))
                .andExpect(
                        status().isNotModified())
                .andExpect(
                        header().string(ETAG, etag))
    }

    def 'should not return file if wasn\'t modified recently'() {
        given:
        Instant lastModified = FileExamples.TXT_FILE.getLastModified()
        expect:
        mockMvc
                .perform(
                        get('/download/' + FileExamples.TXT_FILE_UUID)
                                .header(IF_MODIFIED_SINCE, lastModified))
                .andExpect(
                        status().isNotModified())
    }

    def 'should not return file if server has older version than the client'() {
        given:
        Instant lastModifiedLaterThanServer = FileExamples.TXT_FILE.getLastModified().plusSeconds(60)
        expect:
        mockMvc
                .perform(
                        get('/download/' + FileExamples.TXT_FILE_UUID)
                                .header(IF_MODIFIED_SINCE, lastModifiedLaterThanServer))
                .andExpect(
                        status().isNotModified())
    }

    def 'should return file if was modified after last retrieval'() {
        given:
        Instant lastModifiedRecently = FileExamples.TXT_FILE.getLastModified().minusSeconds(60)
        expect:
        mockMvc
                .perform(
                        get('/download/' + FileExamples.TXT_FILE_UUID)
                                .header(IF_MODIFIED_SINCE, lastModifiedRecently))
                .andExpect(
                        status().isOk())
    }
}
