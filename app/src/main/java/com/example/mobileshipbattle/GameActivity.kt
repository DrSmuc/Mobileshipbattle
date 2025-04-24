package com.example.mobileshipbattle

import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileshipbattle.databinding.ActivityGameBinding
import kotlin.reflect.KMutableProperty0

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
            SetUI()
        }
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



    private fun openned() {
        Toast.makeText(applicationContext, "openned call", Toast.LENGTH_SHORT).show()
        start()
        botBoardSetup()
        // InitializeGrid_p()
        // InitializeGrid_r()
        hideEnd()
        showGuestGrid()

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

        fun isSurroundingEmpty(x: Int, y: Int): Boolean {
            for (dx in -1..1) {
                for (dy in -1..1) {
                    if (dx == 0 && dy == 0) continue  // Skip current cell
                    val nx = x + dx
                    val ny = y + dy
                    if (nx in 0 until size && ny in 0 until size) {
                        if (r_board!![nx][ny] != 0) {
                            return false
                        }
                    }
                }
            }
            return true
        }

        var i = 0
        while (i < 5) {
            val r = (2..4).random()
            val smjer = (1..2).random()

            fun checkVertical(x: Int, y: Int, length: Int): Boolean {
                // Check ship cells and their surroundings
                for (i in 0 until length) {
                    if (r_board!![x + i][y] != 0 || !isSurroundingEmpty(x + i, y)) {
                        return false
                    }
                }
                return true
            }

            fun checkHorizontal(x: Int, y: Int, length: Int): Boolean {
                // Check ship cells and their surroundings
                for (i in 0 until length) {
                    if (r_board!![x][y + i] != 0 || !isSurroundingEmpty(x, y + i)) {
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



    fun SetUI() {
        gameModel?.apply {

        }

        binding.gameStatusTxt.text =
            when(gameModel?.gameStatus) {
                GameStatus.CREATED -> {
                    "Game ID: " + gameModel!!.gameId
                }

                GameStatus.JOINED -> {
                    "Click on start game"
                }

                GameStatus.INPROGRESS -> {
                    gameModel!!.currPlayer + "turn"
                }

                GameStatus.FINISHED -> {
                    if (gameModel!!.winner.isNotEmpty()) gameModel!!.winner + " Won"
                    else "Draw"
                }

                null -> TODO()
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
        openned()
    }

    fun UpdateGameData(model: GameModel) {
        GameData.SaveGameModel(model)
    }

    fun checkForWinner(){

        gameModel?.apply {

            // ships 0


            UpdateGameData(this)

        }


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

            showGuestGrid()

            if (gameStatus != GameStatus.INPROGRESS) {
                Toast.makeText(applicationContext, "Game not started " + clickPos, Toast.LENGTH_SHORT).show()
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