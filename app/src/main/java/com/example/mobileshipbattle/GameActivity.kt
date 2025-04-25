package com.example.mobileshipbattle

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileshipbattle.databinding.ActivityGameBinding

class GameActivity : AppCompatActivity(), View.OnClickListener {

    lateinit var binding: ActivityGameBinding

    private var gameModel: GameModel? = null

    private val gridButtons = mutableListOf<Button>()

    // old converted
    // App.r_board stuff begin
    private var rflagd: Int = 0
    private var rflagc: Int = 0
    private var rflagb: Int = 0
    private var smjer: Int = 0
    private var r: Int = 0
    private var x: Int = 0 // row
    private var y: Int = 0 // col
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

    // ------------array for visual (p1 and p2)----------
    // private Rectangle[,] rectangles_p = new Rectangle[GridSize, GridSize];
    // private Rectangle[,] rectangles_r = new Rectangle[GridSize, GridSize];


    private var shipsRemaining_p: Int = NumberOfShips_p
    private var shipsRemaining_r: Int = NumberOfShips_r

    // ------------global field pos sorage---------------
    var p_board: Array<IntArray>? = null
    var r_board: Array<IntArray>? = null


    private var allowed: Boolean = false
    private var turn: Boolean = false   // true - player fire / false - bot fire
    private var passturn: Boolean = false
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



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        CreateGrid()

        binding.startGameBtn.setOnClickListener {
            StartGame()
        }

        GameData.gameModel.observe(this) {
            gameModel = it
            setUI()
        }

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
                    setBackgroundColor(Color.parseColor("#222E50"))
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

    fun showGuestGrid() {
        val gridSize = 10
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                val index = row * gridSize + col
                val value = r_board?.get(row)?.get(col) ?: 0
                val button = gridButtons[index]
                if (value == 0) {
                    button.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
                } else {
                    button.setBackgroundColor(android.graphics.Color.RED)
                }
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
                if (value == 0) {
                    button.setBackgroundColor(android.graphics.Color.parseColor("#FFFFFF"))
                } else {
                    button.setBackgroundColor(android.graphics.Color.RED)
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

        p_board = Array(ROWS) { IntArray(COLUMNS) { 0 } }
    }

    fun opennedGame() {
        start()
        botBoardSetup()
        // InitializeGrid_p()
        // InitializeGrid_r()
        hideEnd()

        // missSound.setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/water_splash"))
        // hitSound.setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/short_explosion"))
        // backgroundMusic.setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/waves"))
        // winSound.setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/success_fanfare"))
        // losSound.setDataSource(context, Uri.parse("android.resource://${context.packageName}/raw/videogame_death"))
        // backgroundMusic.start()
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
        turn = true
        passturn = false
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
                // Check ship cells and their surroundings
                for (i in 0 until length) {
                    if (r_board!![x + i][y] != 0 || !isSurroundingEmpty(r_board!!, x + i, y)) {
                        return false
                    }
                }
                return true
            }

            fun checkHorizontal(x: Int, y: Int, length: Int): Boolean {
                // Check ship cells and their surroundings
                for (i in 0 until length) {
                    if (r_board!![x][y + i] != 0 || !isSurroundingEmpty(r_board!!, x, y + i)) {
                        return false
                    }
                }
                return true
            }

            if (smjer == 1) { // Vertical placement
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
            } else { // Horizontal placement
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



    fun setUI(){
        gameModel?.apply {

            binding.startGameBtn.visibility = View.VISIBLE

            binding.gameStatusTxt.text =
                when(gameStatus){
                    GameStatus.CREATED -> {
                        binding.startGameBtn.visibility = View.INVISIBLE
                        "Game ID :"+ gameId
                    }
                    GameStatus.JOINED ->{
                        "Click on start game"
                    }
                    GameStatus.INPROGRESS ->{
                        binding.startGameBtn.visibility = View.INVISIBLE
                        when(GameData.myID){
                            currPlayer -> "Your turn"
                            else ->  currPlayer + " turn"
                        }

                    }
                    GameStatus.FINISHED ->{
                        if(winner.isNotEmpty()) {
                            when(GameData.myID){
                                winner -> "You won"
                                else ->   winner + " Won"
                            }

                        }
                        else "DRAW"
                    }
                }

        }
    }


    fun StartGame() {
        gameModel?.apply {
            UpdateGameData(
                GameModel(
                    gameId = gameId,
                    gameStatus =  GameStatus.INPROGRESS
                )
            )
        }
        opennedGame()
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

        // Only allow ship placement if the game is in setup phase
        if (!ready && !placingShip) {
            if (numberOfShipsPlaced < 5) {
                placingShip = true

                // Show dialog to pick ship length (2, 3, or 4)
                val options = arrayOf("2", "3", "4")
                val builder = android.app.AlertDialog.Builder(this)
                builder.setTitle("Odaberi dužinu broda")
                builder.setItems(options) { dialog, which ->
                    shipLength = options[which].toInt()

                    // Check ship count limits
                    if (shipLength == 3 && shipsOfLength3Placed >= 2) {
                        Toast.makeText(this, "Već si postavio maksimalan broj brodova duljine 3 bloka!", Toast.LENGTH_SHORT).show()
                        placingShip = false
                        return@setItems
                    }
                    if (shipLength == 4 && shipsOfLength4Placed >= 1) {
                        Toast.makeText(this, "Već si postavio maksimalan broj brodova duljine 4 bloka!", Toast.LENGTH_SHORT).show()
                        placingShip = false
                        return@setItems
                    }
                    if (shipLength == 2 && shipsOfLength2Placed >= 2) {
                        Toast.makeText(this, "Već si postavio maksimalan broj brodova duljine 2 bloka!", Toast.LENGTH_SHORT).show()
                        placingShip = false
                        return@setItems
                    }

                    // Increment counters
                    when (shipLength) {
                        2 -> shipsOfLength2Placed++
                        3 -> shipsOfLength3Placed++
                        4 -> shipsOfLength4Placed++
                    }
                    numberOfShipsPlaced++

                    // Ask for orientation
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
                            Toast.makeText(this, "Nemoguće postaviti brod! Izvan granica ili mjesto je već zauzeto.", Toast.LENGTH_SHORT).show()
                            placingShip = false
                            numberOfShipsPlaced--
                            when (shipLength) {
                                2 -> shipsOfLength2Placed--
                                3 -> shipsOfLength3Placed--
                                4 -> shipsOfLength4Placed--
                            }
                        }
                    }
                    orientationDialog.show()
                }
                builder.show()
            } else {
                Toast.makeText(this, "Nemoguće postaviti više brodova!", Toast.LENGTH_SHORT).show()
            }
            return
        }
    }


    private fun checkIfShipFits(row: Int, column: Int, horizontal: Boolean): Boolean {
        val requiredLength = shipLength
        val board = p_board ?: return false

        // Check boundaries and cell availability
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
                if (dx == 0 && dy == 0) continue  // Skip current cell
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

        val board = p_board ?: return

        if (horizontal) {
            for (i in column until column + requiredLength) {
                board[row][i] = putLength
                val idx = row * 10 + i
                gridButtons[idx].setBackgroundColor(Color.RED)
            }
        } else {
            for (i in row until row + requiredLength) {
                board[i][column] = putLength
                val idx = i * 10 + column
                gridButtons[idx].setBackgroundColor(Color.RED)
            }
        }
    }


    private fun ready_f() {
        // You can set UI elements to visible/enabled here as needed
        ready = true
        // For example:
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
            val clickPos = (v?.tag as String).toInt()

            if (gameStatus == GameStatus.JOINED) {
                val row = clickPos / 10
                val column = clickPos % 10

                Toast.makeText(applicationContext, "" + row + " " + column, Toast.LENGTH_SHORT).show()

                selectedShipPos(row, column)
            }

            if (gameStatus != GameStatus.INPROGRESS) {
                // Toast.makeText(applicationContext, "Game not started " + clickPos, Toast.LENGTH_SHORT).show()
                return
            }

            if (true) {  // PROMJENIT: ako pukne di prije nije
                // handle pucanje

                currPlayer = if (currPlayer == "Host") "Guest" else "Host"
                UpdateGameData(this)

            }
        }
    }
}