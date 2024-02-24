package in.handyman.raven.lib.agadia.outbound.delivery.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import in.handyman.raven.lambda.doa.audit.ActionExecutionAudit;
import in.handyman.raven.lib.OutboundDeliveryNotifyAction;

public interface OutboundInterface {

    String requestApiCaller(final OutboundDeliveryNotifyAction.TableInputQuerySet tableInputQuerySet);

    ObjectNode outboundFileOptions(final OutboundDeliveryNotifyAction.TableInputQuerySet tableInputQuerySet);
}
