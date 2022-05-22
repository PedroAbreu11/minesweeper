import kotlin.random.Random

const val invalido = "Invalid response.\n"
const val lines = "lines" // isto só está aqui por causa do aviso

fun main() {
    val input = inputMenu()
    if (input) {
        val nome = introduzirNome()
        val mostrarLegenda = mostrarLegenda()
        val linhas = gameSettings(lines)
        val colunas = gameSettings("columns")
        val mines = mines() ?: calculateNumMinesForGameConfiguration(linhas, colunas)!!

        val terreno = createMatrixTerrain(linhas, colunas, mines)
        fillNumberOfMines(terreno)
        var gameOver = false
        var gameOverStatus = false // falso se o jogador perdeu, true se o jogador ganhou
        var showEverything = false
        var currentCoord = Pair(0, 0) // coordenada onde o jogador se situa atualmente
        var currentCoordData = Pair(countNumberOfMinesCloseToCurrentCell(terreno, 0, 0).toString(), true) // dados do quadrado na coordenada acima

        do {
            revealMatrix(terreno, currentCoord.first, currentCoord.second) // atualizar os quadrados visíveis
            println(makeTerrain(terreno, mostrarLegenda, showEverything=showEverything)) // dar print do terreno
            if (showEverything) {
                showEverything = false // desativar o showEverything
            }
            var currentCoordTemp: Pair<Int, Int>? = null // recebe o input

            // INPUT DE DADOS

            do {
                var flag = false
                println("Choose the Target cell (e.g 2D)")
                val respostaTemp = readLine()

                if (respostaTemp == "abracadabra") {
                    currentCoordTemp = null
                    flag = true
                } else if (respostaTemp == "exit") {
                    return
                }

                val coordinates = getCoordinates(respostaTemp)

                if (coordinates != null && isMovementPValid(currentCoord, coordinates) && isCoordinateInsideTerrain(coordinates, colunas, linhas)) {
                    currentCoordTemp = coordinates
                    flag = true
                } else {
                    print(invalido)
                    println(makeTerrain(terreno, mostrarLegenda, showEverything=showEverything))
                }
            } while (!flag)

            // FIM DO INPUT DE DADOS

            if (currentCoordTemp != null) {
                // o input é uma coordenada

                terreno[currentCoord.first][currentCoord.second] = currentCoordData // mete o texto de volta ao quadrado onde estava o jogador
                currentCoord = currentCoordTemp

                val texto = terreno[currentCoord.first][currentCoord.second].first
                val visivel = terreno[currentCoord.first][currentCoord.second].second
                currentCoordData = Pair(texto, visivel) // dados do quadrado onde o jogador se vai mover

                terreno[currentCoord.first][currentCoord.second] = Pair("P", true) // coloca o jogador no quadrado

                if (currentCoordData.first == "*") {
                    // o quadrado é uma bomba, o jogador perdeu o jogo
                    gameOver = true
                    gameOverStatus = false
                } else if (currentCoordData.first == "f") {
                    // o quadrado é o quadrado final, o jogador ganhou o jogo
                    gameOver = true
                    gameOverStatus = true
                }
            } else {
                showEverything = true // o input é "abracadabra", ativar o showEverything
            }
        } while (!gameOver)

        println(makeTerrain(terreno, mostrarLegenda, showEverything=true))
        if (gameOverStatus) {
            println("You win the game!")
        } else {
            println("You lost the game!")
        }
    }
}

fun inputMenu(): Boolean {
    var resposta: Boolean? = null

    while (resposta == null) {
        println(makeMenu())
        val respostaTemp = readLine()?.toIntOrNull()

        if (respostaTemp != null) {
            if (respostaTemp == 1) {
                resposta = true
            } else if (respostaTemp == 0) {
                resposta = false
            }
        }
        else {
            println(invalido)
        }
    }

    return resposta
}

fun introduzirNome(): String {
    var nome: String? = null
    while (nome == null) {
        println("Enter player name?")
        val nomeTemp = readLine()

        if (isNameValid(nomeTemp)) {
            nome = nomeTemp!!
        } else {
            println(invalido)
        }
    }

    return nome
}

fun mostrarLegenda(): Boolean {
    var resposta: Boolean? = null
    while (resposta == null) {
        println("Show legend (y/n)?")
        val respostaTemp = readLine()?.toUpperCase()

        if (respostaTemp != null) {
            if (respostaTemp == "Y") {
                resposta = true
            } else if (respostaTemp == "N") {
                resposta = false
            }
        }
        else {
            println(invalido)
        }
    }

    return resposta
}

fun gameSettings(mensagem: String): Int {
    var setting: Int? = null
    while (setting == null) {
        println("How many $mensagem?")
        val settingTemp = readLine()?.toIntOrNull()

        if (settingTemp != null && settingTemp > 0) {
            setting = settingTemp
        }
        else {
            println(invalido)
        }
    }

    return setting
}

fun mines(): Int? {
    while (true) {
        println("How many mines (press enter for default value)?")
        val input = readLine()
        val mines = input?.toIntOrNull()

        if (mines != null && mines > 0) {
            return mines
        } else if (input != null) {
            if (mines == null && input.isEmpty()) { // verificar se é só um enter
                return null
            }
        }
        println(invalido)
    }
}

fun makeMenu(): String {
    return "\nWelcome to DEISI Minesweeper\n\n1 - Start New Game\n0 - Exit Game\n"
}

fun isNameValid(name: String?, minLength: Int = 3): Boolean {
    var index = 0
    var primeiroNome = true
    var primeiraLetra = true // é true se estiver a próxima letra é a primeira de um nome
    var nomes = 0

    if (name == null) {
        return false
    }

    while (index < name.length) {
        if (primeiraLetra && name[index] != ' ') { // se estiver no início de um nome

            if (!name[index].isUpperCase()) { // verificar se a primeira letra é maiúscula
                return false
            }

            nomes++
            primeiraLetra = false
        }
        if (name[index] == ' ') { // quando/enquanto for um espaço

            if (primeiroNome && index < minLength) { // verficar o tamanho do primeiro nome
                return false
            }

            primeiroNome = false
            primeiraLetra = true
        }

        index++
    }

    return nomes >= 2
}

fun calculateNumMinesForGameConfiguration(numLines: Int, numColumns: Int): Int? {
    val casasVazias = (numLines * numColumns - 2)

    if (casasVazias in 14..20) {
        return 6
    }
    if (casasVazias in 21..40) {
        return 9
    }
    if (casasVazias in 41..60) {
        return 12
    }
    if (casasVazias in 61..79) {
        return 19
    }
    else {
        return null
    }
}

fun isValidGameMinesConfiguration(numLines: Int, numColumns: Int, numMines: Int): Boolean {
    if (numMines <= 0 || numMines > (numLines * numColumns - 2)) {
        return false
    }

    return true
}

fun createLegend(numColumns: Int): String {
    var string = ""
    var coluna = numColumns
    var curChar = 'A'

    while (coluna > 0) {
        string += "$curChar"
        if (coluna != 1) { // adicionar espaços a não ser que seja a última letra
            string += "   "
        }

        curChar++
        coluna--
    }

    return string
}

fun makeTerrain(matrixTerrain: Array<Array<Pair<String, Boolean>>>,
                showLegend: Boolean = false,
                withColor: Boolean = false,
                showEverything: Boolean = false): String {

    // esta função usa os valores máximos dos array a dobrar, para usar um sistema de par/ímpar para desenhar
    // as linhas dos quadrados e as linhas separadoras

    var finalString = ""
    var tamanhoString = 0

    var corString = ""
    var endLegendColor = ""
    // as variáveis começam em branco para poderem ser usadas mesmo que não seja para mostrar a cor

    if (showLegend && withColor) {
        val esc: String = "\u001B"
        corString = "$esc[97;44m"
        endLegendColor = "$esc[0m"
    }

    if (showLegend) {
        val legenda = "    ${createLegend(matrixTerrain[0].size)}    "
        tamanhoString = legenda.length

        finalString += "$corString$legenda$endLegendColor\n"
    }

    for (y in 0..(matrixTerrain.size - 1) * 2) {
        var string = ""
        if (y % 2 != 0) {

            if (showLegend) {
                string += "$corString   $endLegendColor"
            }

            for (x in 0 until matrixTerrain[0].size) {
                if (x == matrixTerrain[0].size - 1) {
                    string += "---"
                } else {
                    string += "---+"
                }
            }
        } else {
            val indiceY = (y.toDouble() / 2 + 0.5).toInt()

            if (showLegend) {
                string += "$corString ${indiceY+1} $endLegendColor"
            }

            for (x in 0..(matrixTerrain[0].size - 1) * 2) {
                if (x % 2 == 0) {
                    val indiceX = (x.toDouble() / 2 + 0.5).toInt()

                    if (!showEverything
                        && matrixTerrain[(y.toDouble() / 2 + 0.5).toInt()][(x.toDouble() / 2 + 0.5).toInt()].second
                        || showEverything) {

                        string += " ${matrixTerrain[indiceY][indiceX].first} "
                    } else {
                        string += "   "
                    }
                } else {
                    string += "|"
                }
            }
        }

        if (showLegend) {
            string += "$corString   $endLegendColor"
        }

        if (!showLegend && y < (matrixTerrain.size - 1) * 2 || showLegend) {
            string += "\n"
        }

        finalString += string
    }

    if (showLegend) {
        finalString += corString
        for (x in 0 until tamanhoString) {
            finalString += " "
        }
        finalString += endLegendColor
    }

    return finalString
}

fun createMatrixTerrain(numLines: Int,
                        numColumns: Int,
                        numMines: Int,
                        ensurePathToWin: Boolean = false): Array<Array<Pair<String, Boolean>>> {

    val array: Array<Array<Pair<String, Boolean>>> = Array(numLines) { Array(numColumns) { Pair(" ", false) } }

    array[0][0] = Pair("P", true)
    array[numLines-1][numColumns-1] = Pair("f", true)

    // Parte das minas

    if (isValidGameMinesConfiguration(numLines, numColumns, numMines)) {
        for (m in 0 until numMines) {
            var nextCycle = true

            while (nextCycle) {
                val randomY = Random.nextInt(numLines)
                val randomX = Random.nextInt(numColumns)

                if (ensurePathToWin) {
                    // Semi-Aleatório

                    val squareAround = getSquareAroundPoint(randomY, randomX, numLines, numColumns)

                    if (isEmptyAround(array,
                            randomY,
                            randomX,
                            squareAround.first.first,
                            squareAround.first.second,
                            squareAround.second.first,
                            squareAround.second.second)
                    ) {
                        if (array[randomY][randomX].first != "*"
                            && !(randomY == 0 && randomX == 0)
                            && !(randomY == numLines - 1 && randomX == numColumns - 1)
                        ) {
                            array[randomY][randomX] = Pair("*", false)
                            nextCycle = false
                        }
                    }
                } else {
                    // Completamente aleatório

                    if (array[randomY][randomX].first != "*"
                        && !(randomY == 0 && randomX == 0)
                        && !(randomY == numLines - 1 && randomX == numColumns - 1)
                    ) {
                        array[randomY][randomX] = Pair("*", false)
                        nextCycle = false
                    }
                }
            }
        }
    }

    return array
}

fun fillNumberOfMines(matrixTerrain: Array<Array<Pair<String, Boolean>>>) {
    for (y in 0 until matrixTerrain.size) {
        for (x in 0 until matrixTerrain[y].size) {
            val cell = matrixTerrain[y][x]

            if (cell.first == " ") {
                val numberOfMines = countNumberOfMinesCloseToCurrentCell(matrixTerrain, y, x)
                if (numberOfMines != 0) {
                    matrixTerrain[y][x] = Pair(numberOfMines.toString(), false)
                }
            }
        }
    }
}

fun countNumberOfMinesCloseToCurrentCell(matrixTerrain: Array<Array<Pair<String, Boolean>>>,
                                         centerY: Int,
                                         centerX: Int): Int {

    val square = getSquareAroundPoint(centerY, centerX, matrixTerrain.size, matrixTerrain[0].size)

    val yTop = square.first.first
    val xTop = square.first.second
    val yBottom = square.second.first
    val xBottom = square.second.second

    var numMines = 0

    for (y in yTop..yBottom) {
        for (x in xTop..xBottom) {
            if (!(x == centerX && y == centerY)) {
                if (matrixTerrain[y][x].first == "*") {
                    numMines++
                }
            }
        }
    }

    return numMines
}

fun revealMatrix(matrixTerrain: Array<Array<Pair<String, Boolean>>>,
                 coordY: Int,
                 coordX: Int,
                 endGame: Boolean = false) {

    val squareAroundPlayer = getSquareAroundPoint(coordY, coordX, matrixTerrain.size, matrixTerrain[0].size)

    val squareYTop = squareAroundPlayer.first.first
    val squareXTop = squareAroundPlayer.first.second
    val squareYBottom = squareAroundPlayer.second.first
    val squareXBottom = squareAroundPlayer.second.second


    for (y in squareYTop..squareYBottom) {
        for (x in squareXTop..squareXBottom) {
            if (endGame) {
                matrixTerrain[y][x] = Pair(matrixTerrain[y][x].first, true)
            } else if (!endGame && matrixTerrain[y][x].first != "*") {
                matrixTerrain[y][x] = Pair(matrixTerrain[y][x].first, true)
            }
        }
    }
}

fun isEmptyAround(matrixTerrain: Array<Array<Pair<String, Boolean>>>,
                  centerY: Int,
                  centerX: Int,
                  y1: Int,
                  x1: Int,
                  yr: Int,
                  xr: Int): Boolean {

    for (y in y1..yr) {
        for (x in x1..xr) {
            if (!(x == centerX && y == centerY)) {
                if (matrixTerrain[y][x].first == "*" || matrixTerrain[y][x].first == "P" || matrixTerrain[y][x].first == "f") {
                    return false
                }
            }
        }
    }

    return true
}

fun isMovementPValid(currentCoord: Pair<Int, Int>, targetCoord: Pair<Int, Int>): Boolean {

    val movimentoX = currentCoord.first - targetCoord.first
    val movimentoY = currentCoord.second - targetCoord.second

    return movimentoX <= 1 && movimentoX >= -1 && movimentoY <= 1 && movimentoY >= -1
}

fun isCoordinateInsideTerrain(coord: Pair<Int, Int>, numColumns: Int, numLines: Int): Boolean {
    return coord.first < numColumns - 1 && coord.first >= 0 && coord.second < numLines - 1 && coord.second >= 0
}

fun getCoordinates(readText: String?): Pair<Int, Int>? {
    if (readText != null && readText.length == 2 && readText[0].toString().toIntOrNull() != null) {
        return Pair(readText[0].toInt() - '1'.toInt(), readText[1].toUpperCase().toInt() - 'A'.toInt())
    } else {
        return null
    }
}

fun getSquareAroundPoint(linha: Int,
                         coluna: Int,
                         numLinesTemp: Int,
                         numColumnsTemp: Int): Pair<Pair<Int, Int>, Pair<Int, Int>> {
    val numLines = numLinesTemp - 1
    val numColumns = numColumnsTemp - 1

    // os pontos a seguir são os cantos superior esquerdo e inferior direito

    var pontoLinhaXL = linha - 1
    var pontoLinhaXR = coluna - 1
    var pontoColunaXL = linha + 1
    var pontoColunaXR = coluna + 1

    // caso o ponto esteja junto a uma "parede" ou num canto

    if (linha == 0) {
        pontoLinhaXL = linha
    }
    if (numLines == linha) {
        pontoColunaXL = linha
    }
    if (coluna == 0) {
        pontoLinhaXR = coluna
    }
    if (numColumns == coluna) {
        pontoColunaXR = coluna
    }


    return Pair(Pair(pontoLinhaXL, pontoLinhaXR), Pair(pontoColunaXL, pontoColunaXR))
}