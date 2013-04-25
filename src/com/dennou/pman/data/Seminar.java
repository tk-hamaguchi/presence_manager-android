package com.dennou.pman.data;

import java.util.Date;

public class Seminar {
	private String name;
	private Date startedAt;
	private Date endedAt;
	private String description;
	private String url;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getEndedAt() {
		return endedAt;
	}
	public void setEndedAt(Date endedAt) {
		this.endedAt = endedAt;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public Date getStartedAt() {
		return startedAt;
	}
	public void setStartedAt(Date startedAt) {
		this.startedAt = startedAt;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
}
