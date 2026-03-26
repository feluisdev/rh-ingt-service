package cv.igrp.RH_Service.sigdi.domain.compliance.models;

import cv.igrp.RH_Service.sigdi.application.constants.NotificationChannel;
import cv.igrp.RH_Service.sigdi.domain.compliance.valueobject.NotificationLogId;
import lombok.Getter;

@Getter
public class NotificationLog {

  private final NotificationLogId id;
  private final String recipient;
  private final NotificationChannel channel;
  private final String subject;
  private final String sentAt;
  private final String status;

  private NotificationLog(NotificationLogId id, String recipient, NotificationChannel channel,
                          String subject, String sentAt, String status) {
    if (id == null) throw new IllegalArgumentException("id é obrigatório");
    if (recipient == null || recipient.isBlank()) throw new IllegalArgumentException("recipient é obrigatório");
    this.id = id;
    this.recipient = recipient;
    this.channel = channel;
    this.subject = subject;
    this.sentAt = sentAt;
    this.status = status;
  }

  public static NotificationLog create(String recipient, NotificationChannel channel, String subject) {
    return new NotificationLog(NotificationLogId.gerarNovo(), recipient, channel, subject, null, null);
  }

  public static NotificationLog reconstruct(NotificationLogId id, String recipient,
                                            NotificationChannel channel, String subject,
                                            String sentAt, String status) {
    return new NotificationLog(id, recipient, channel, subject, sentAt, status);
  }
}

