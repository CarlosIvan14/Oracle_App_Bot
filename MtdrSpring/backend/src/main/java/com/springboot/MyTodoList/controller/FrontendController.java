package com.springboot.MyTodoList.controller;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FrontendController {

	@RequestMapping(value = { "/", "/home", "/profile", "/projects/**", "/{path:^(?!api|static|assets|.*\\.).*$}/**" })
	public String forward(HttpServletRequest request) {
		// Log the original request
		System.out.println("Original URI: " + request.getRequestURI());
		System.out.println("Forwarding to index.html");
		return "forward:/index.html";
	}

}