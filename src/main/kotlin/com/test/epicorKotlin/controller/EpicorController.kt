package com.test.epicorKotlin.controller

import com.test.epicorKotlin.model.Response
import com.test.epicorKotlin.service.EpicorService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/epicor/kotlin")
class EpicorController(private val epicorService: EpicorService) {

    private val logger = LoggerFactory.getLogger(EpicorController::class.java)

    @GetMapping("/call")
    fun callURL(@RequestParam(required = false) url :String?): ResponseEntity<Response>{
        logger.info("URL: {}", url)
        return try {
            val responseRes = epicorService.fileRead(url)
            ResponseEntity.ok(responseRes)
        } catch (e: Exception) {
            logger.error("Error processing request", e)
            ResponseEntity.status(500).build()
        }
    }

}