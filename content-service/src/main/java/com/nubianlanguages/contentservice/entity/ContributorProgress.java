package com.nubianlanguages.contentservice.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "contrib_progress")
public class ContributorProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CONTRIBUTOR_ID", unique = true, nullable = false)
    private Long contributorId;

    private int numberOfRecordings;

    public ContributorProgress() {}

    public Long getId() {
        return id;
    }

    public Long getContributorId() {
        return contributorId;
    }

    public void setContributorId(Long contributorId) {
        this.contributorId = contributorId;
    }

    public int getNumberOfRecordings() {
        return numberOfRecordings;
    }

    public void setNumberOfRecordings(int numberOfRecordings) {
        this.numberOfRecordings = numberOfRecordings;
    }
}