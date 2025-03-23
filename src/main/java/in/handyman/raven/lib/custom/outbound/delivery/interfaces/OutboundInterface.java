package in.handyman.raven.lib.custom.outbound.delivery.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.lib.custom.outbound.delivery.entity.TableInputQuerySet;

public interface OutboundInterface {

    String requestApiCaller(final TableInputQuerySet tableInputQuerySet);

    ObjectNode outboundFileOptions(final TableInputQuerySet tableInputQuerySet);
}
