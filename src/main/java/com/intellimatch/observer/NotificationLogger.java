package com.intellimatch.observer;

import com.intellimatch.model.MatchResult;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Concrete Observer: logs all match notifications to an in-memory notification feed.
 * Simulates real-time notification delivery to both applicants and recruiters.
 */
public class NotificationLogger implements MatchObserver {

    private static final Logger LOG = Logger.getLogger(NotificationLogger.class.getName());
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final List<String> notificationFeed = new ArrayList<>();

    @Override
    public void onMatchFound(MatchResult result) {
        String timestamp = LocalDateTime.now().format(DTF);

        String applicantNotif = String.format(
            "[%s] APPLICANT ALERT — %s: New match with %s (%s) — Score: %s | Matched Skills: %s",
            timestamp,
            result.getApplicant().getName(),
            result.getRecruiter().getInternshipTitle(),
            result.getRecruiter().getCompany(),
            result.getScorePercentage(),
            result.getMatchedSkills()
        );

        String recruiterNotif = String.format(
            "[%s] RECRUITER ALERT — %s @ %s: Candidate %s matched your posting — Score: %s | Missing Skills: %s",
            timestamp,
            result.getRecruiter().getName(),
            result.getRecruiter().getCompany(),
            result.getApplicant().getName(),
            result.getScorePercentage(),
            result.getMissingSkills()
        );

        notificationFeed.add(applicantNotif);
        notificationFeed.add(recruiterNotif);

        LOG.info(applicantNotif);
        LOG.info(recruiterNotif);
    }

    public List<String> getNotificationFeed() {
        return Collections.unmodifiableList(notificationFeed);
    }

    public void clearFeed() {
        notificationFeed.clear();
    }
}
