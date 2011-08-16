package com.amee.engine;

//@Service
//public class AMEENotifier {
//
//    private final Log log = LogFactory.getLog(getClass());
//
//    private static final String to = " sx5g8t@twittermail.com";
//    private MailSender mailSender;
//
//    @Transactional
//    public void echo() {
//        log.info("echo()");
//
//        SimpleMailMessage msg = new SimpleMailMessage();
//        msg.setTo(to);
//        msg.setText("hello from AMEE");
//        try{
//            this.mailSender.send(msg);
//        }
//        catch(MailException ex) {
//            log.error(ex.getMessage());
//        }
//    }
//
//    public void setMailSender(MailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//}
