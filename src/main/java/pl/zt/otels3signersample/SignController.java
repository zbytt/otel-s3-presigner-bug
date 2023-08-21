package pl.zt.otels3signersample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import software.amazon.awssdk.services.s3.S3Client;

@RestController
public class SignController {

    private static final Logger LOG = LoggerFactory.getLogger(SignController.class);

    @Autowired
    Signer signer;
    @Autowired
    S3Client s3Client;

    @GetMapping("/first")
    public ResponseEntity<Response> generateUrl(@RequestHeader("x-amzn-trace-id") String traceID) {
        try {
            s3Client.headObject(builder -> builder.bucket("test").key("test"));
        } catch (Exception e) {
            LOG.info("Exception: " + e);
        }

        signer.presignPutObject();

        LOG.info("Header received: " + traceID);
        return ResponseEntity.ok(new Response(traceID));
    }

    @GetMapping("/second")
    ResponseEntity<Response> any(@RequestHeader("x-amzn-trace-id") String traceID) {
        LOG.info("Header received: " + traceID);
        return ResponseEntity.ok(new Response(traceID));
    }

    public record Response(String traceId) {}
}
