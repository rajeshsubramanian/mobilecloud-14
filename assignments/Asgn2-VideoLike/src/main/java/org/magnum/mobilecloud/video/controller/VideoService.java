/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video.controller;

import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;

@Controller
public class VideoService {

	@Autowired
	private VideoRepository videos;

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> getVideoList() {
		return Lists.newArrayList(videos.findAll());
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
	public @ResponseBody Video getVideoById(@PathVariable("id") long id) {
		return videos.findOne(id);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video video) {
		return videos.save(video);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public @ResponseBody void likeVideo(@PathVariable("id") long id,
			Principal p, HttpServletResponse response) throws IOException {

		Video video = videos.findOne(id);
		if (video == null) {
			throw new ResourceNotFoundException();
		}

		String userName = p.getName();
		if (video.getUsersLikingThisVideo() != null
				&& video.getUsersLikingThisVideo().contains(userName)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}
		List<String> usersLikingThisVideo = video.getUsersLikingThisVideo();
		if (usersLikingThisVideo == null) {
			usersLikingThisVideo = new ArrayList<String>();
			video.setUsersLikingThisVideo(usersLikingThisVideo);
		}
		usersLikingThisVideo.add(userName);
		video.setLikes(video.getLikes() + 1);
		videos.save(video);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
	public @ResponseBody void unlikeVideo(@PathVariable("id") long id,
			Principal p, HttpServletResponse response) throws IOException {

		Video video = videos.findOne(id);
		if (video == null) {
			throw new ResourceNotFoundException();
		}
		String userName = p.getName();
		List<String> usersLikingThisVideo = video.getUsersLikingThisVideo();
		if (usersLikingThisVideo != null
				&& !usersLikingThisVideo.contains(userName)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST);
			return;
		}

		usersLikingThisVideo.remove(userName);
		video.setUsersLikingThisVideo(usersLikingThisVideo);
		video.setLikes(video.getLikes() - 1);
		videos.save(video);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByTitle(
			@RequestParam(value = VideoSvcApi.TITLE_PARAMETER) String title) {

		return videos.findByName(title);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	public @ResponseBody Collection<Video> findByDurationLessThan(
			@RequestParam(value = VideoSvcApi.DURATION_PARAMETER) long duration) {

		return videos.findByDurationLessThan(duration);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	public @ResponseBody Collection<String> getUsersWhoLikedVideo(
			@PathVariable("id") long id) {

		Video video = videos.findOne(id);
		if (video == null) {
			throw new ResourceNotFoundException();
		}
		return video.getUsersLikingThisVideo();
	}
}
