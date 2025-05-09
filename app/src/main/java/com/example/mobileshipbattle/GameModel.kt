package com.example.mobileshipbattle

import kotlin.random.Random

//
//  0 - empty
//  1 - hit
//
//
//
//
//
//
//
//
//

data class GameModel (
    var gameId: String = "-1",
    var hostFieldPos: MutableList<String> = mutableListOf(
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", ""),
    var guestFieldPos: MutableList<String> = mutableListOf(
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", "",
        "", "", "", "", "", "", "", "", "", ""),
    var winner: String = "",
    var gameStatus: GameStatus = GameStatus.CREATED,
    var currPlayer: String = (arrayOf("Host", "Guest"))[Random.nextInt(2)]
)


enum class GameStatus{
    CREATED,
    JOINED,
    INPROGRESS,
    FINISHED
}

