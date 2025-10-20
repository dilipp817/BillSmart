package com.autobill.billsmart.Controlers

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody
import jakarta.servlet.http.HttpServletRequest

@Controller
class HomeController {
    @RequestMapping("home")
    @ResponseBody
    fun home(request: HttpServletRequest): String {
        val name = request.getParameter("name")
        println("hello$name from controller")
        return "Hello$name I'm here and working fine"
    }
}
