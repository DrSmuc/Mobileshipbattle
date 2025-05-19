package com.example.mobileshipbattle

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract.Colors
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileshipbattle.databinding.ActivityGameBinding
import com.google.firebase.database.collection.LLRBNode

class GameActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityGameBinding

    private var gameModel: GameModel? = null

    private val gridButtons = mutableListOf<Button>()

    private val handler = Handler(Looper.getMainLooper())

    // old converted
    // App.r_board stuff begin
    private var rflagd: Int = 0
    private var rflagc: Int = 0
    private var rflagb: Int = 0
    private var smjer: Int = 0
    private var r: Int = 0
    private var x: Int = 0
    private var y: Int = 0
    private var pastHit: Int = 0
    private var direction: Int = 0
    private var firstHitX: Int = 0
    private var firstHitY: Int = 0
    private var lastHitX: Int = 0
    private var lastHitY: Int = 0
    // counters
    private var p_br2: Int = 0
    private var p_br3: Int = 0
    private var p_br4: Int = 0
    private var p_br5: Int = 0
    private var p_br6: Int = 0
    private var p_br7: Int = 0

    private var r_br2: Int = 2
    private var r_br3: Int = 3
    private var r_br4: Int = 4
    private var r_br5: Int = 5
    private var r_br6: Int = 2
    private var r_br7: Int = 3

    private val NumberOfShips_p: Int = 5
    private val NumberOfShips_r: Int = 5

    // ------------array for visual (p1, p2)----------
    // private Rectangle[,] rectangles_p = new Rectangle[GridSize, GridSize];
    // private Rectangle[,] rectangles_r = new Rectangle[GridSize, GridSize];


    private var shipsRemaining_p: Int = NumberOfShips_p
    private var shipsRemaining_r: Int = NumberOfShips_r

    // ------------global field pos sorage---------------
    var p_board: Array<IntArray>? = null
    var r_board: Array<IntArray>? = null


    private var allowed: Boolean = false
    private var endgame: Int = 0


    //----------------------------------- setup -------------------------------------------

    val ROWS = 10
    val COLUMNS = 10

    private var placingShip = false
    private var shipLength = 0
    private var numberOfShipsPlaced = 0
    private var shipsOfLength2Placed = 0
    private var shipsOfLength3Placed = 0
    private var shipsOfLength4Placed = 0

    private var b6 = false
    private var b7 = false

    private var ready = false

    val color_water = Color.rgb(0, 211, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        p_board = Array(ROWS) { IntArray(COLUMNS) { 0 } }
        r_board = Array(ROWS) { IntArray(COLUMNS) { 0 } }

        GameData.fetchGameModel()

        CreateGrid()

        binding.startGameBtn.setOnClickListener {
            StartGame()
        }

        binding.resetSetupBtn.setOnClickListener {
            resetSetup()
        }

        GameData.gameModel.observe(this) {
            gameModel = it
            setUI()

            // Check if both players are ready and game should start
            if (gameModel?.gameId != "-1" &&
                gameModel?.gameStatus == GameStatus.JOINED &&
                gameModel?.hostReady == true &&
                gameModel?.guestReady == true) {

                gameModel?.gameStatus = GameStatus.INPROGRESS
                UpdateGameData(gameModel!!)
                opennedGame()
            }
        }

        handleGameStateChange()

        opennedSetup()
    }

    fun CreateGrid() {
        val gridLayout = GridLayout(this).apply {
            rowCount = 10
            columnCount = 10
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            useDefaultMargins = false
            setPadding(0, 0, 0, 0)
            clipToPadding = false
            alignmentMode = GridLayout.ALIGN_BOUNDS
        }

        binding.gridContainer.addView(gridLayout)

        binding.gridContainer.post {
            val gridSize = 10
            val lineThickness = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 1f, resources.displayMetrics
            ).toInt()
            val gridWidth = binding.gridContainer.width
            val buttonSize = (gridWidth - (lineThickness * (gridSize + 1))) / gridSize

            for (i in 0 until gridSize * gridSize) {
                val row = i / gridSize
                val col = i % gridSize

                val left = lineThickness
                val top = lineThickness
                val right = if (col == gridSize - 1) lineThickness else 0
                val bottom = if (row == gridSize - 1) lineThickness else 0

                val params = GridLayout.LayoutParams().apply {
                    width = buttonSize
                    height = buttonSize
                    rowSpec = GridLayout.spec(row)
                    columnSpec = GridLayout.spec(col)
                    setMargins(left, top, right, bottom)
                }

                val button = Button(this).apply {
                    layoutParams = params
                    setBackgroundColor(color_water)
                    text = ""
                    tag = i.toString()
                    id = View.generateViewId()
                    setPadding(0, 0, 0, 0)
                    setOnClickListener(this@GameActivity)
                }

                gridButtons.add(button)
                gridLayout.addView(button)
            }
        }
    }

    fun showHostGrid() {
        val gridSize = 10
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val index = row * gridSize + col
                val value = p_board?.get(row)?.get(col) ?: 0
                val button = gridButtons[index]

                when (value) {
                    0 -> button.setBackgroundColor(color_water) // empty
                    1 -> button.setBackgroundColor(Color.DKGRAY) // missed
                    in 2..7 -> button.setBackgroundColor(Color.GREEN) // not hit
                    in 12..17 -> button.setBackgroundColor(Color.RED) // hit
                    -1 -> button.setBackgroundColor(Color.BLACK) // sinked
                }
            }
        }
    }

    fun showGuestGrid() {
        val gridSize = 10
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val index = row * gridSize + col
                val value = r_board?.get(row)?.get(col) ?: 0
                val button = gridButtons[index]

                when (value) {
                    0, in 2..7 -> button.setBackgroundColor(color_water) // empty/unknown
                    1 -> button.setBackgroundColor(Color.DKGRAY) // missed
                    in 12..17 -> button.setBackgroundColor(Color.RED) // hit
                    -1 -> button.setBackgroundColor(Color.BLACK) // sinked
                }
            }
        }
    }

    fun showHostGridHidden() {
        val gridSize = 10
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val index = row * gridSize + col
                val value = p_board?.get(row)?.get(col) ?: 0
                val button = gridButtons[index]

                when (value) {
                    0, in 2..7 -> button.setBackgroundColor(color_water) // empty/unknown
                    1 -> button.setBackgroundColor(Color.DKGRAY) // missed
                    in 12..17 -> button.setBackgroundColor(Color.RED) // hit
                    -1 -> button.setBackgroundColor(Color.BLACK) // sinked
                }
            }
        }
    }

    fun showGuestGridVisible() {
        val gridSize = 10
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val index = row * gridSize + col
                val value = r_board?.get(row)?.get(col) ?: 0
                val button = gridButtons[index]

                when (value) {
                    0 -> button.setBackgroundColor(color_water) // empty
                    1 -> button.setBackgroundColor(Color.DKGRAY) // missed
                    in 2..7 -> button.setBackgroundColor(Color.GREEN) // not hit
                    in 12..17 -> button.setBackgroundColor(Color.RED) // hit
                    -1 -> button.setBackgroundColor(Color.BLACK) // sinked
                }
            }
        }
    }


    fun opennedSetup() {
        numberOfShipsPlaced = 0
        shipsOfLength2Placed = 0
        shipsOfLength3Placed = 0
        shipsOfLength4Placed = 0
        b6 = false
        b7 = false
        ready = false
        placingShip = false

        if (gameModel?.gameId == "-1") {
            GameData.myID = "Host"
            p_board = Array(ROWS) { IntArray(COLUMNS) { 0 } }
            r_board = Array(ROWS) { IntArray(COLUMNS) { 0 } }
        } else {
            if (GameData.myID == "Host") {
                p_board = Array(ROWS) { IntArray(COLUMNS) { 0 } }
            } else {
                r_board = Array(ROWS) { IntArray(COLUMNS) { 0 } }
            }
        }

        binding.startGameBtn.visibility = View.VISIBLE
        binding.resetSetupBtn.visibility = View.VISIBLE
    }


    fun opennedGame() {
        start()
        if (gameModel!!.gameId == "-1") {
            // offline
            botBoardSetup()
            showGuestGrid()
            allowed = true
            gameModel!!.currPlayer = "Host"
            GameData.myID = "Host"
        } else {
            // online
            updateUIForCurrentPlayer()
        }
        hideEnd()
    }


    private fun hideEnd() {
        // endstatus.visibility = View.GONE
        // rematch_b.visibility = View.GONE
        // home_b.visibility = View.GONE
        // myCanvas.visibility = View.GONE
        // myCanvas.alpha = 0f
    }

    fun start() {
        allowed = true
        endgame = 0

        rflagd = 0
        rflagc = 0
        rflagb = 0
        pastHit = 0
        direction = 0

        p_br2 = 0
        p_br3 = 0
        p_br4 = 0
        p_br5 = 0
        p_br6 = 0
        p_br7 = 0

        r_br2 = 2
        r_br3 = 3
        r_br4 = 4
        r_br5 = 5
        r_br6 = 2
        r_br7 = 3

        shipsRemaining_p = NumberOfShips_p
        shipsRemaining_r = NumberOfShips_r

        // -----global fieldPos----------
        p_board = convertFieldPosTo2DArray(gameModel!!.hostFieldPos)
        r_board = convertFieldPosTo2DArray(gameModel!!.guestFieldPos)
    }

    fun botBoardSetup() {
        val size = 10
        r_board = Array(size) { IntArray(size) { 0 } }

        var i = 0
        while (i < 5) {
            val r = (2..4).random()
            val smjer = (1..2).random()

            fun checkVertical(x: Int, y: Int, length: Int): Boolean {
                for (i in 0 until length) {
                    if (r_board!![x + i][y] != 0 || !isSurroundingEmpty(r_board!!, x + i, y)) {
                        return false
                    }
                }
                return true
            }

            fun checkHorizontal(x: Int, y: Int, length: Int): Boolean {
                for (i in 0 until length) {
                    if (r_board!![x][y + i] != 0 || !isSurroundingEmpty(r_board!!, x, y + i)) {
                        return false
                    }
                }
                return true
            }

            if (smjer == 1) { // vertical
                when (r) {
                    4 -> if (rflagd == 0) {
                        val x = (0..6).random()
                        val y = (0..9).random()
                        if (checkVertical(x, y, 4)) {
                            for (i in 0..3) r_board!![x + i][y] = 4
                            rflagd++
                            i++
                        }
                    }
                    3 -> when (rflagc) {
                        0, 1 -> {
                            val x = (0..7).random()
                            val y = (0..9).random()
                            if (checkVertical(x, y, 3)) {
                                val value = if (rflagc == 0) 3 else 7
                                for (i in 0..2) r_board!![x + i][y] = value
                                rflagc++
                                i++
                            }
                        }
                    }
                    2 -> when (rflagb) {
                        0, 1 -> {
                            val x = (0..8).random()
                            val y = (0..9).random()
                            if (checkVertical(x, y, 2)) {
                                val value = if (rflagb == 0) 2 else 6
                                for (i in 0..1) r_board!![x + i][y] = value
                                rflagb++
                                i++
                            }
                        }
                    }
                }
            } else { // horizontal
                when (r) {
                    4 -> if (rflagd == 0) {
                        val x = (0..9).random()
                        val y = (0..6).random()
                        if (checkHorizontal(x, y, 4)) {
                            for (i in 0..3) r_board!![x][y + i] = 4
                            rflagd++
                            i++
                        }
                    }
                    3 -> when (rflagc) {
                        0, 1 -> {
                            val x = (0..9).random()
                            val y = (0..7).random()
                            if (checkHorizontal(x, y, 3)) {
                                val value = if (rflagc == 0) 3 else 7
                                for (i in 0..2) r_board!![x][y + i] = value
                                rflagc++
                                i++
                            }
                        }
                    }
                    2 -> when (rflagb) {
                        0, 1 -> {
                            val x = (0..9).random()
                            val y = (0..8).random()
                            if (checkHorizontal(x, y, 2)) {
                                val value = if (rflagb == 0) 2 else 6
                                for (i in 0..1) r_board!![x][y + i] = value
                                rflagb++
                                i++
                            }
                        }
                    }
                }
            }
        }
        gameModel!!.guestFieldPos = r_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()
    }



    fun setUI() {
        gameModel?.apply {
            binding.startGameBtn.visibility = View.VISIBLE
            binding.resetSetupBtn.visibility = View.VISIBLE
            binding.gameStatusTxt.text = when(gameStatus) {
                GameStatus.CREATED -> {
                    binding.startGameBtn.visibility = View.INVISIBLE
                    binding.resetSetupBtn.visibility = View.INVISIBLE
                    "Game ID: " + gameId
                }
                GameStatus.JOINED -> {
                    if (GameData.myID == "Host") {
                        if (hostReady) {
                            "Waiting for Guest to start..."
                        } else {
                            "Place your ships and start game"
                        }
                    } else { // Guest
                        if (guestReady) {
                            "Waiting for Host to start..."
                        } else {
                            "Place your ships and start game"
                        }
                    }
                }
                GameStatus.INPROGRESS -> {
                    binding.startGameBtn.visibility = View.INVISIBLE
                    binding.resetSetupBtn.visibility = View.INVISIBLE

                    when(GameData.myID) {
                        currPlayer -> "Your turn"
                        else -> "$currPlayer's turn"
                    }
                }
                GameStatus.FINISHED -> {
                    binding.startGameBtn.visibility = View.INVISIBLE
                    binding.resetSetupBtn.visibility = View.INVISIBLE

                    if (winner.isNotEmpty()) {
                        if (winner == GameData.myID) {
                            showGameOver(true)
                            "You won"
                        } else {
                            showGameOver(false)
                            "$winner Won"
                        }
                    } else {
                        "DRAW"
                    }
                }
            }
        }
    }


    fun StartGame() {
        if (numberOfShipsPlaced < 5) {
            Toast.makeText(this@GameActivity, "Please place all your ships first!", Toast.LENGTH_LONG).show()
            return
        }

        gameModel?.apply {
            if (gameId == "-1") {
                hostFieldPos = p_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()
                currPlayer = "Host"
                UpdateGameData(
                    GameModel(
                        gameId = gameId,
                        gameStatus = GameStatus.INPROGRESS,
                        currPlayer = currPlayer,
                        hostFieldPos = hostFieldPos,
                        guestFieldPos = guestFieldPos
                    )
                )
                opennedGame()
            } else {
                binding.startGameBtn.isEnabled = false
                binding.startGameBtn.text = "Saving..."

                if (GameData.myID == "Host") {
                    hostFieldPos = p_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()
                    Toast.makeText(this@GameActivity, "" + p_board!![0][0], Toast.LENGTH_SHORT).show()
                } else {
                    guestFieldPos = r_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()
                }

                Toast.makeText(this@GameActivity, "" + hostFieldPos[0], Toast.LENGTH_SHORT).show()

                UpdateGameData(this)

                handler.postDelayed({
                    if (GameData.myID == "Host") {
                        hostReady = true
                    } else {
                        guestReady = true
                    }

                    UpdateGameData(this)
                    binding.startGameBtn.text = "Waiting..."

                    if (hostReady && guestReady && false) {
                        gameStatus = GameStatus.INPROGRESS
                        Toast.makeText(this@GameActivity, "Game starting! Both players are ready.", Toast.LENGTH_SHORT).show()
                        UpdateGameData(this)
                        opennedGame()
                    } else {
                        Toast.makeText(this@GameActivity, "Waiting for other player to start the game...", Toast.LENGTH_SHORT).show()
                    }

                    binding.startGameBtn.visibility = View.INVISIBLE
                    binding.resetSetupBtn.visibility = View.INVISIBLE
                }, 1000)
            }
        }
    }

    fun resetSetup() {
        if (GameData.myID == "Host" || gameModel?.gameId == "-1") {
            p_board = Array(ROWS) { IntArray(COLUMNS) { 0 } }
        } else {
            r_board = Array(ROWS) { IntArray(COLUMNS) { 0 } }
        }

        numberOfShipsPlaced = 0
        shipsOfLength2Placed = 0
        shipsOfLength3Placed = 0
        shipsOfLength4Placed = 0
        b6 = false
        b7 = false
        ready = false
        placingShip = false

        for (i in 0 until ROWS * COLUMNS) {
            gridButtons[i].setBackgroundColor(color_water)
        }
    }


    fun UpdateGameData(model: GameModel) {
        GameData.saveGameModel(model)
    }

    fun checkForWinner(){

        gameModel?.apply {

            // ships 0


            UpdateGameData(this)

        }
    }


    fun selectedShipPos(row: Int, column: Int) {
        if (!ready && !placingShip) {
            if (numberOfShipsPlaced < 5) {
                placingShip = true
                val options = arrayOf("2", "3", "4")
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Odaberi duÅ¾inu broda")
                builder.setItems(options) { dialog, which ->
                    shipLength = options[which].toInt()

                    if (shipLength == 3 && shipsOfLength3Placed >= 2) {
                        Toast.makeText(this, "VeÄ‡ si postavio maksimalan broj brodova duljine 3 bloka!", Toast.LENGTH_SHORT).show()
                        placingShip = false
                        return@setItems
                    }

                    if (shipLength == 4 && shipsOfLength4Placed >= 1) {
                        Toast.makeText(this, "VeÄ‡ si postavio maksimalan broj brodova duljine 4 bloka!", Toast.LENGTH_SHORT).show()
                        placingShip = false
                        return@setItems
                    }

                    if (shipLength == 2 && shipsOfLength2Placed >= 2) {
                        Toast.makeText(this, "VeÄ‡ si postavio maksimalan broj brodova duljine 2 bloka!", Toast.LENGTH_SHORT).show()
                        placingShip = false
                        return@setItems
                    }

                    when (shipLength) {
                        2 -> shipsOfLength2Placed++
                        3 -> shipsOfLength3Placed++
                        4 -> shipsOfLength4Placed++
                    }

                    numberOfShipsPlaced++
                    val orientationOptions = arrayOf("Horizontalno", "Vertikalno")
                    val orientationDialog = android.app.AlertDialog.Builder(this)
                    orientationDialog.setTitle("Odaberi smjer broda")
                    orientationDialog.setItems(orientationOptions) { _, orientationWhich ->
                        val isHorizontal = (orientationWhich == 0)

                        if (checkIfShipFits(row, column, isHorizontal)) {
                            placeShip(row, column, isHorizontal)
                            placingShip = false
                            if (numberOfShipsPlaced == 5) {
                                ready_f()
                            }
                        } else {
                            Toast.makeText(this, "NemoguÄ‡e postaviti brod! Izvan granica ili mjesto je veÄ‡ zauzeto.", Toast.LENGTH_SHORT).show()
                            placingShip = false
                            numberOfShipsPlaced--
                            when (shipLength) {
                                2 -> shipsOfLength2Placed--
                                3 -> shipsOfLength3Placed--
                                4 -> shipsOfLength4Placed--
                            }
                        }
                    }

                    orientationDialog.setOnCancelListener {
                        // if canceled direction
                        placingShip = false
                        numberOfShipsPlaced--
                        when (shipLength) {
                            2 -> shipsOfLength2Placed--
                            3 -> shipsOfLength3Placed--
                            4 -> shipsOfLength4Placed--
                        }
                    }

                    orientationDialog.show()
                }

                builder.setOnCancelListener {
                    // if canceled size
                    placingShip = false
                }

                builder.show()
            } else {
                Toast.makeText(this, "NemoguÄ‡e postaviti viÅ¡e brodova!", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun checkIfShipFits(row: Int, column: Int, horizontal: Boolean): Boolean {
        val requiredLength = shipLength

        val board = if (GameData.myID == "Host" || gameModel?.gameId == "-1") {
            p_board
        } else {
            r_board
        } ?: return false

        if (horizontal) {
            if (column + requiredLength > COLUMNS) return false
            for (i in column until column + requiredLength) {
                if (board[row][i] != 0 || !isSurroundingEmpty(board, row, i)) {
                    return false
                }
            }
        } else {
            if (row + requiredLength > ROWS) return false
            for (i in row until row + requiredLength) {
                if (board[i][column] != 0 || !isSurroundingEmpty(board, i, column)) {
                    return false
                }
            }
        }
        return true
    }

    private fun isSurroundingEmpty(board: Array<IntArray>, x: Int, y: Int): Boolean {
        for (dx in -1..1) {
            for (dy in -1..1) {
                if (dx == 0 && dy == 0) continue
                val nx = x + dx
                val ny = y + dy
                if (nx in 0 until ROWS && ny in 0 until COLUMNS) {
                    if (board[nx][ny] != 0) {
                        return false
                    }
                }
            }
        }
        return true
    }



    private fun placeShip(row: Int, column: Int, horizontal: Boolean) {
        val requiredLength = shipLength
        var putLength = shipLength

        if (shipLength == 2 && !b6) {
            b6 = true
        } else if (shipLength == 2 && b6) {
            putLength = 6
            b6 = false
        } else if (shipLength == 3 && !b7) {
            b7 = true
        } else if (shipLength == 3 && b7) {
            putLength = 7
            b7 = false
        }

        val board = if (GameData.myID == "Host" || gameModel?.gameId == "-1") {
            p_board
        } else {
            r_board
        } ?: return

        if (horizontal) {
            for (i in column until column + requiredLength) {
                board[row][i] = putLength
                val idx = row * 10 + i
                gridButtons[idx].setBackgroundColor(Color.GREEN)
            }
        } else {
            for (i in row until row + requiredLength) {
                board[i][column] = putLength
                val idx = i * 10 + column
                gridButtons[idx].setBackgroundColor(Color.GREEN)
            }
        }
    }

    private fun ready_f() {
        ready = true

        // binding.continueButton.visibility = View.VISIBLE
        // binding.savePresetButton.visibility = View.VISIBLE
    }


    private fun convertFieldPosTo2DArray(fieldPos: List<String>): Array<IntArray> {
        val size = 10
        return Array(size) { row ->
            IntArray(size) { col ->
                val value = fieldPos[row * size + col]
                value.toIntOrNull() ?: 0
            }
        }
    }

    override fun onClick(v: View?) {
        gameModel?.apply {
            val clickPos = (v?.tag as? String)?.toIntOrNull() ?: return
            val row = clickPos / 10
            val column = clickPos % 10

            if (gameStatus == GameStatus.JOINED) {
                selectedShipPos(row, column)
                return
            }

            if (gameStatus != GameStatus.INPROGRESS) return

            if (currPlayer == GameData.myID && allowed) {
                allowed = false

                if (gameId == "-1") {
                    // offline
                    when (val cellValue = r_board?.get(row)?.get(column) ?: 0) {
                        0 -> handleMiss(row, column)
                        in 2..7 -> handleHit(row, column, cellValue)
                        1, in 12..17, -1 -> {
                            allowed = true
                            Toast.makeText(
                                this@GameActivity,
                                "VeÄ‡ pucano ovdje",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    // online
                    if (GameData.myID == "Host") {
                        when (val cellValue = r_board?.get(row)?.get(column) ?: 0) {
                            0 -> handleOnlineMiss(row, column)
                            in 2..7 -> handleOnlineHit(row, column, cellValue)
                            1, in 12..17, -1 -> {
                                allowed = true
                                Toast.makeText(
                                    this@GameActivity,
                                    "Already fired here",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        // Guest player
                        when (val cellValue = p_board?.get(row)?.get(column) ?: 0) {
                            0 -> handleOnlineMiss(row, column)
                            in 2..7 -> handleOnlineHit(row, column, cellValue)
                            1, in 12..17, -1 -> {
                                allowed = true
                                Toast.makeText(
                                    this@GameActivity,
                                    "Already fired here",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }



    private fun handleHit(row: Int, col: Int, shipType: Int) {
        r_board?.let { board ->
            board[row][col] = shipType + 10
            gridButtons[row * 10 + col].setBackgroundColor(Color.RED)

            when (shipType) {
                2 -> r_br2--
                3 -> r_br3--
                4 -> r_br4--
                6 -> r_br6--
                7 -> r_br7--
            }

            val remaining = when (shipType) {
                2 -> r_br2
                3 -> r_br3
                4 -> r_br4
                6 -> r_br6
                7 -> r_br7
                else -> 1
            }

            if (remaining == 0) {
                shipsRemaining_r--
                sink(shipType)
            }

            if (endgame == 0) {
                gameModel!!.currPlayer = "Host"
                allowed = true
            } else {
                allowed = false
            }
        }
    }

    private fun handleMiss(row: Int, col: Int) {
        r_board?.let {
            if (it[row][col] != 0) return
            it[row][col] = 1
            gridButtons[row * 10 + col].setBackgroundColor(Color.DKGRAY)

            continue_f()
        }
    }


    fun botTurn() {
        allowed = false

        var x = 0
        var y = 0

        if (pastHit == 0) {
            var limit = 500
            do {
                x = (0..9).random()
                y = (0..9).random()
                limit--
            } while (isOne(p_board!!, x, y) && limit > 0)
        } else {
            if (direction == 0) {
                direction = (1..4).random()
            }

            if (pastHit == 2) {
                when (direction) {
                    1 -> { // left
                        if (lastHitY == 0 || p_board?.get(lastHitX)?.get(lastHitY - 1) == 1 ||
                            p_board?.get(lastHitX)?.get(lastHitY - 1) in 12..17 ||
                            p_board?.get(lastHitX)?.get(lastHitY - 1) == -1) {
                            direction = 3
                            pastHit = 1
                            lastHitX = firstHitX
                            lastHitY = firstHitY
                        }
                    }
                    2 -> { // up
                        if (lastHitX == 0 || p_board?.get(lastHitX - 1)?.get(lastHitY) == 1 ||
                            p_board?.get(lastHitX - 1)?.get(lastHitY) in 12..17 ||
                            p_board?.get(lastHitX - 1)?.get(lastHitY) == -1) {
                            direction = 4
                            pastHit = 1
                            lastHitX = firstHitX
                            lastHitY = firstHitY
                        }
                    }
                    3 -> { // right
                        if (lastHitY == 9 || p_board?.get(lastHitX)?.get(lastHitY + 1) == 1 ||
                            p_board?.get(lastHitX)?.get(lastHitY + 1) in 12..17 ||
                            p_board?.get(lastHitX)?.get(lastHitY + 1) == -1) {
                            direction = 1
                            pastHit = 1
                            lastHitX = firstHitX
                            lastHitY = firstHitY
                        }
                    }
                    4 -> { // down
                        if (lastHitX == 9 || p_board?.get(lastHitX + 1)?.get(lastHitY) == 1 ||
                            p_board?.get(lastHitX + 1)?.get(lastHitY) in 12..17 ||
                            p_board?.get(lastHitX + 1)?.get(lastHitY) == -1) {
                            direction = 2
                            pastHit = 1
                            lastHitX = firstHitX
                            lastHitY = firstHitY
                        }
                    }
                }
            }

            when (direction) {
                1 -> { // left
                    if (lastHitY > 0) {
                        x = lastHitX
                        y = lastHitY - 1
                    } else {
                        direction = 0
                        botTurn()
                        return
                    }
                }
                2 -> { // up
                    if (lastHitX > 0) {
                        x = lastHitX - 1
                        y = lastHitY
                    } else {
                        direction = 0
                        botTurn()
                        return
                    }
                }
                3 -> { // right
                    if (lastHitY < 9) {
                        x = lastHitX
                        y = lastHitY + 1
                    } else {
                        direction = 0
                        botTurn()
                        return
                    }
                }
                4 -> { // down
                    if (lastHitX < 9) {
                        x = lastHitX + 1
                        y = lastHitY
                    } else {
                        direction = 0
                        botTurn()
                        return
                    }
                }
            }
        }

        p_board?.let { board ->
            val cellValue = board[x][y]

            if (cellValue == 1 || cellValue in 12..17 || cellValue == -1) {
                direction = 0
                botTurn()
                return
            }

            if (cellValue in 2..7) {
                // hit

                lastHitX = x
                lastHitY = y
                if (pastHit == 0) {
                    firstHitX = x
                    firstHitY = y
                }

                board[x][y] = cellValue + 10

                val idx = x * 10 + y
                gridButtons[idx].setBackgroundColor(Color.RED)

                when (cellValue) {
                    2 -> {
                        p_br2++
                        if (p_br2 < 2) {
                            pastHit = 1
                        } else {
                            pastHit = 0
                            direction = 0
                            shipsRemaining_p--
                            sinkPlayerShip(2)
                        }
                    }
                    3 -> {
                        p_br3++
                        if (p_br3 < 3) {
                            pastHit = if (p_br3 == 2) 2 else 1
                        } else {
                            pastHit = 0
                            direction = 0
                            shipsRemaining_p--
                            sinkPlayerShip(3)
                        }
                    }
                    4 -> {
                        p_br4++
                        if (p_br4 < 4) {
                            pastHit = if (p_br4 > 1) 2 else 1
                        } else {
                            pastHit = 0
                            direction = 0
                            shipsRemaining_p--
                            sinkPlayerShip(4)
                        }
                    }
                    5 -> {
                        p_br5++
                        if (p_br5 < 5) {
                            pastHit = if (p_br5 > 1) 2 else 1
                        } else {
                            pastHit = 0
                            direction = 0
                            shipsRemaining_p--
                            sinkPlayerShip(5)
                        }
                    }
                    6 -> {
                        p_br6++
                        if (p_br6 < 2) {
                            pastHit = 1
                        } else {
                            pastHit = 0
                            direction = 0
                            shipsRemaining_p--
                            sinkPlayerShip(6)
                        }
                    }
                    7 -> {
                        p_br7++
                        if (p_br7 < 3) {
                            pastHit = if (p_br7 == 2) 2 else 1
                        } else {
                            pastHit = 0
                            direction = 0
                            shipsRemaining_p--
                            sinkPlayerShip(7)
                        }
                    }
                }

                if (shipsRemaining_p == 0) {
                    endgame = 2
                    Toast.makeText(this@GameActivity, "You lost! All your ships are sunk.", Toast.LENGTH_LONG).show()
                }

                handler.postDelayed({
                    if (endgame == 0) botTurn()
                }, 1000)

            } else {
                // miss

                board[x][y] = 1
                val idx = x * 10 + y
                gridButtons[idx].setBackgroundColor(Color.DKGRAY)

                if (pastHit == 2) {
                    direction = when (direction) {
                        1 -> 3
                        2 -> 4
                        3 -> 1
                        4 -> 2
                        else -> 0
                    }
                    lastHitX = firstHitX
                    lastHitY = firstHitY
                    pastHit = 1
                } else {
                    direction = 0
                }

                continue_f()
            }
        }
    }

    private fun isOne(board: Array<IntArray>, row: Int, col: Int): Boolean {
        val directions = arrayOf(
            Pair(-1, 0),  // up
            Pair(0, 1),   // right
            Pair(1, 0),   // down
            Pair(0, -1)   // left
        )

        for ((dx, dy) in directions) {
            val newRow = row + dx
            val newCol = col + dy

            if (newRow < 0 || newRow >= 10 || newCol < 0 || newCol >= 10) {
                continue
            }

            if (board[newRow][newCol] != 1) {
                return false
            }
        }

        return true
    }


    private fun sink(shipType: Int) {
        r_board?.let { board ->
            val shipCells = mutableListOf<Pair<Int, Int>>()

            for (i in 0 until 10) {
                for (j in 0 until 10) {
                    if (board[i][j] == shipType + 10) {
                        board[i][j] = -1
                        val idx = i * 10 + j
                        gridButtons[idx].setBackgroundColor(Color.BLACK)
                        shipCells.add(Pair(i, j))
                    }
                }
            }

            for (cell in shipCells) {
                for (dx in -1..1) {
                    for (dy in -1..1) {
                        if (dx == 0 && dy == 0) continue

                        val nx = cell.first + dx
                        val ny = cell.second + dy

                        if (nx in 0 until 10 && ny in 0 until 10) {
                            if (board[nx][ny] == 0 || board[nx][ny] in 2..7) {
                                board[nx][ny] = 1
                                val idx = nx * 10 + ny
                                gridButtons[idx].setBackgroundColor(Color.DKGRAY)
                            }
                        }
                    }
                }
            }

            if (shipsRemaining_r == 0) {
                endgame = 1
                showGameOver(true) // win
            }
        }
    }

    private fun sinkPlayerShip(shipType: Int) {
        p_board?.let { board ->
            val shipCells = mutableListOf<Pair<Int, Int>>()

            for (i in 0 until 10) {
                for (j in 0 until 10) {
                    if (board[i][j] == shipType + 10) {
                        board[i][j] = -1
                        val idx = i * 10 + j
                        gridButtons[idx].setBackgroundColor(Color.BLACK)
                        shipCells.add(Pair(i, j))
                    }
                }
            }

            for (cell in shipCells) {
                for (dx in -1..1) {
                    for (dy in -1..1) {
                        if (dx == 0 && dy == 0) continue

                        val nx = cell.first + dx
                        val ny = cell.second + dy

                        if (nx in 0 until 10 && ny in 0 until 10) {
                            if (board[nx][ny] == 0 || board[nx][ny] in 2..7) {
                                board[nx][ny] = 1
                                val idx = nx * 10 + ny
                                gridButtons[idx].setBackgroundColor(Color.DKGRAY)
                            }
                        }
                    }
                }
            }
        }

        if (shipsRemaining_p == 0) {
            endgame = -1
            showGameOver(false) // loss
        }
    }



    // -------------------------- online handling -----------------------------


    private fun handleGameStateChange() {
        GameData.gameModel.observe(this) { model ->
            if (model == null) return@observe

            val previousGameStatus = gameModel?.gameStatus
            val previousTurn = gameModel?.currPlayer
            gameModel = model

            if (model.gameStatus == GameStatus.FINISHED && endgame == 0) {
                endgame = if (model.winner == GameData.myID) 1 else 2
                showGameOver(model.winner == GameData.myID)
            }

            if (model.gameId != "-1") {

                if (previousGameStatus != GameStatus.INPROGRESS && model.gameStatus == GameStatus.INPROGRESS) {
                    if (GameData.myID == "Host") {
                        model.hostFieldPos = p_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()
                    } else {
                        model.guestFieldPos = r_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()
                    }


                    UpdateGameData(model)

                    opennedGame()
                }

                if (previousTurn != model.currPlayer && model.gameStatus == GameStatus.INPROGRESS) {
                    updateUIForCurrentPlayer()

                    Toast.makeText(this@GameActivity,
                        if (model.currPlayer == GameData.myID) "Your turn" else "${model.currPlayer}'s turn",
                        Toast.LENGTH_SHORT).show()
                }

                if (model.gameStatus == GameStatus.INPROGRESS) {
                    p_board = convertFieldPosTo2DArray(model.hostFieldPos)
                    r_board = convertFieldPosTo2DArray(model.guestFieldPos)
                    updateUIForCurrentPlayer()

                    if (model.currPlayer == GameData.myID) {
                        handler.postDelayed({
                            allowed = true
                        }, 1500)
                    } else {
                        allowed = false
                    }
                }
            }

            setUI()
        }
    }

    private fun handleGameAbandon() {
        onBackPressedDispatcher.addCallback(this) {
            if (gameModel?.gameId != "-1" && gameModel?.gameStatus == GameStatus.INPROGRESS) {
                val builder = android.app.AlertDialog.Builder(this@GameActivity)
                builder.setTitle("Abandon Game")
                builder.setMessage("Are you sure you want to abandon this game? You will forfeit the match.")
                builder.setPositiveButton("Yes") { _, _ ->
                    gameModel?.apply {
                        gameStatus = GameStatus.FINISHED
                        winner = if (GameData.myID == "Host") "Guest" else "Host"
                        UpdateGameData(this)
                        finish()
                    }
                }
                builder.setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                builder.show()
            } else {
                finish()
            }
        }
    }


    private fun handleOnlineHit(row: Int, col: Int, shipType: Int) {
        allowed = false

        if (GameData.myID == "Host") {
            r_board?.let { board ->
                board[row][col] = shipType + 10
                gridButtons[row * 10 + col].setBackgroundColor(Color.RED)

                var shipSunk = false
                var remainingCells = 0

                for (i in 0 until 10) {
                    for (j in 0 until 10) {
                        if (board[i][j] == shipType) {
                            remainingCells++
                        }
                    }
                }

                if (remainingCells == 0) {
                    shipSunk = true
                    sinkOnlineShip(shipType)
                }

                gameModel?.apply {
                    guestFieldPos = r_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()

                    if (shipSunk) {
                        var allShipsSunk = true
                        for (i in 0 until 10) {
                            for (j in 0 until 10) {
                                if (r_board!![i][j] in 2..7) {
                                    allShipsSunk = false
                                    break
                                }
                            }
                            if (!allShipsSunk) break
                        }

                        if (allShipsSunk) {
                            gameStatus = GameStatus.FINISHED
                            winner = "Host"
                        }
                    }

                    UpdateGameData(this)

                    handler.postDelayed({
                        if (gameStatus == GameStatus.FINISHED) {
                            showGameOver(winner == GameData.myID)
                        } else {
                            showGuestGrid()
                            Toast.makeText(this@GameActivity, "You hit a ship! Shoot again.", Toast.LENGTH_SHORT).show()

                            // Re-enable interaction
                            // allowed = true
                        }
                    }, 0)
                }
            }
        } else {
            p_board?.let { board ->
                board[row][col] = shipType + 10
                gridButtons[row * 10 + col].setBackgroundColor(Color.RED)

                var shipSunk = false
                var remainingCells = 0

                for (i in 0 until 10) {
                    for (j in 0 until 10) {
                        if (board[i][j] == shipType) {
                            remainingCells++
                        }
                    }
                }

                if (remainingCells == 0) {
                    shipSunk = true
                    sinkOnlineShip(shipType)
                }

                gameModel?.apply {
                    hostFieldPos = p_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()

                    if (shipSunk) {
                        var allShipsSunk = true
                        for (i in 0 until 10) {
                            for (j in 0 until 10) {
                                if (p_board!![i][j] in 2..7) {
                                    allShipsSunk = false
                                    break
                                }
                            }
                            if (!allShipsSunk) break
                        }

                        if (allShipsSunk) {
                            gameStatus = GameStatus.FINISHED
                            winner = "Guest"
                        }
                    }

                    UpdateGameData(this)

                    handler.postDelayed({
                        if (gameStatus == GameStatus.FINISHED) {
                            showGameOver(winner == GameData.myID)
                        } else {
                            showHostGridHidden()
                            binding.gameStatusTxt.text = "Your turn!"
                            Toast.makeText(this@GameActivity, "You hit a ship! Shoot again.", Toast.LENGTH_SHORT).show()

                            allowed = true
                        }
                    }, 0)
                }
            }
        }
    }

    private fun handleOnlineMiss(row: Int, col: Int) {
        allowed = false

        if (GameData.myID == "Host") {
            r_board?.let { board ->
                board[row][col] = 1
                gridButtons[row * 10 + col].setBackgroundColor(Color.DKGRAY)

                gameModel?.apply {
                    guestFieldPos = r_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()

                    UpdateGameData(this)

                    handler.postDelayed({
                        currPlayer = "Guest"
                        UpdateGameData(this)

                        showHostGrid()
                        binding.gameStatusTxt.text = "Guest's turn"
                        Toast.makeText(this@GameActivity, "You missed! Guest's turn now.", Toast.LENGTH_SHORT).show()
                        allowed = false
                    }, 1500)
                }
            }
        } else {
            p_board?.let { board ->
                board[row][col] = 1
                gridButtons[row * 10 + col].setBackgroundColor(Color.DKGRAY)

                gameModel?.apply {
                    hostFieldPos = p_board!!.flatMap { it.toList() }.map { it.toString() }.toMutableList()

                    UpdateGameData(this)

                    handler.postDelayed({
                        currPlayer = "Host"
                        UpdateGameData(this)

                        showGuestGridVisible()
                        binding.gameStatusTxt.text = "Host's turn"
                        Toast.makeText(this@GameActivity, "You missed! Host's turn now.", Toast.LENGTH_SHORT).show()
                        allowed = false
                    }, 1500)
                }
            }
        }
    }


    private fun sinkOnlineShip(shipType: Int) {
        val board = if (GameData.myID == "Host") r_board else p_board

        board?.let {
            for (i in 0 until 10) {
                for (j in 0 until 10) {
                    if (it[i][j] == shipType + 10) {
                        it[i][j] = -1
                        val idx = i * 10 + j
                        gridButtons[idx].setBackgroundColor(Color.BLACK)

                        for (dx in -1..1) {
                            for (dy in -1..1) {
                                if (dx == 0 && dy == 0) continue
                                val nx = i + dx
                                val ny = j + dy
                                if (nx in 0 until 10 && ny in 0 until 10) {
                                    if (it[nx][ny] == 0) {
                                        it[nx][ny] = 1
                                        val surroundIdx = nx * 10 + ny
                                        gridButtons[surroundIdx].setBackgroundColor(Color.DKGRAY)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateUIForCurrentPlayer() {
        if (gameModel?.gameId != "-1") {
            if (GameData.myID == "Host") {
                if (gameModel?.currPlayer == "Host") {
                    showGuestGrid()
                    // allowed = true
                } else {
                    showHostGrid()
                    allowed = false
                }
            } else {
                // Guest player
                if (gameModel?.currPlayer == "Guest") {
                    showHostGridHidden()
                    // allowed = true
                } else {
                    showGuestGridVisible()
                    allowed = false
                }
            }

            binding.gameStatusTxt.text = when(GameData.myID) {
                gameModel?.currPlayer -> "Your turn"
                else -> "${gameModel?.currPlayer}'s turn"
            }
        }
    }


    fun continue_f() {
        allowed = false
        handler.postDelayed({
            gameModel?.apply {
                currPlayer = if (currPlayer == "Host") "Guest" else "Host"
                UpdateGameData(this)

                if (gameModel?.gameId == "-1") {
                    if (currPlayer == "Host") {
                        showGuestGrid()
                        allowed = true
                    } else {
                        showHostGrid()
                        handler.postDelayed({
                            botTurn()
                        }, 500)
                    }
                } else {
                    updateUIForCurrentPlayer()
                }
            }
        }, 2000)
    }

    // ------------------------------------------ endgame logic ---------------------------------------------------

    private fun showGameOver(win: Boolean) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.game_over_popup, null)

        val width = LinearLayout.LayoutParams.MATCH_PARENT
        val height = LinearLayout.LayoutParams.MATCH_PARENT
        val focusable = true
        val popupWindow = PopupWindow(popupView, width, height, focusable)

        val resultText = popupView.findViewById<TextView>(R.id.gameResultText)
        if (win) {
            resultText.text = "VICTORY"
            resultText.setTextColor(Color.parseColor("#4CAF50")) // Green
        } else {
            resultText.text = "DEFEAT"
            resultText.setTextColor(Color.parseColor("#F44336")) // Red
        }

        val boardContainer = popupView.findViewById<FrameLayout>(R.id.opponentBoardContainer)
        createOpponentFinalBoard(boardContainer)

        popupView.findViewById<Button>(R.id.rematchButton).setOnClickListener {
            popupWindow.dismiss()
            resetGame()
        }

        popupView.findViewById<Button>(R.id.exitButton).setOnClickListener {
            popupWindow.dismiss()
            finish()
        }

        popupWindow.showAtLocation(binding.root, Gravity.CENTER, 0, 0)
    }

    private fun createOpponentFinalBoard(container: FrameLayout) {
        val gridLayout = GridLayout(this).apply {
            rowCount = 10
            columnCount = 10
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
            useDefaultMargins = false
            setPadding(0, 0, 0, 0)
            alignmentMode = GridLayout.ALIGN_BOUNDS
        }

        container.addView(gridLayout)

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels - 100
        val buttonSize = screenWidth / 10

        val finalButtons = mutableListOf<Button>()

        for (i in 0 until 100) {
            val row = i / 10
            val col = i % 10

            val params = GridLayout.LayoutParams().apply {
                width = buttonSize
                height = buttonSize
                rowSpec = GridLayout.spec(row)
                columnSpec = GridLayout.spec(col)
                setMargins(1, 1, 1, 1)
            }

            val button = Button(this).apply {
                layoutParams = params
                setPadding(0, 0, 0, 0)
                isEnabled = false
            }

            finalButtons.add(button)
            gridLayout.addView(button)
        }

        r_board?.let { board ->
            for (row in 0 until 10) {
                for (col in 0 until 10) {
                    val index = row * 10 + col
                    val value = board[row][col]
                    val button = finalButtons[index]

                    when (value) {
                        0 -> button.setBackgroundColor(color_water) // Empty water
                        1 -> button.setBackgroundColor(Color.DKGRAY) // Missed shot
                        in 2..7 -> button.setBackgroundColor(Color.GREEN) // Unhit ship (now visible)
                        in 12..17 -> button.setBackgroundColor(Color.YELLOW) // Hit ship
                        -1 -> button.setBackgroundColor(Color.RED) // Sunk ship
                    }
                }
            }
        }
    }

    private fun resetGame() {

        if (gameModel!!.gameId == "-1") {
            resetSetup()

            endgame = 0
            allowed = true

            shipsRemaining_p = NumberOfShips_p
            shipsRemaining_r = NumberOfShips_r

            pastHit = 0
            direction = 0

            p_br2 = 0
            p_br3 = 0
            p_br4 = 0
            p_br5 = 0
            p_br6 = 0
            p_br7 = 0

            r_br2 = 2
            r_br3 = 3
            r_br4 = 4
            r_br5 = 5
            r_br6 = 2
            r_br7 = 3

            gameModel?.apply {
                gameStatus = GameStatus.JOINED
                currPlayer = "Host"
                hostFieldPos = mutableListOf()
                guestFieldPos = mutableListOf()

                UpdateGameData(this)
            }

            opennedSetup()

            binding.startGameBtn.visibility = View.VISIBLE
            binding.resetSetupBtn.visibility = View.VISIBLE
        }
    }

}