package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_about.buttonBack
import kotlinx.android.synthetic.main.activity_calc_simple.*

class CalcSimple : AppCompatActivity() {
    var resultText = "0"
    private val operators = setOf("+", "-", "*", "/")
    private val NUMBER_REGEX = Regex("-?([0-9]+(\\.[0-9]+)?|Infinity)")
    private val LAST_NUMBER_REGEX = Regex("-?([0-9]+(\\.[0-9]+)?|Infinity)$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calc_simple)
        setEventListeners()
    }

    private fun addElementToResultText(element: String) {
        if (resultText in listOf("NaN", "Infinity", "-Infinity")) {
            resultText = "0"
        }

        when {
            isOperator(element) -> handleOperator(element)
            isDotSymbol(element) -> handleDot()
            isDigit(element) -> handleDigit(element)
            isEqualitySymbol(element) -> calculateResult()
            isInverseSymbol(element) -> invertResult()
            isClearSymbol(element) -> clearResult()
            isDeleteSymbol(element) -> deleteLastSymbol()
        }

        display.hint = resultText
    }

    private fun deleteLastSymbol() {
        if (resultText.length > 1) {
            val lastIndex = resultText.lastIndex
            if (resultText[lastIndex] == ' ') {
                resultText = resultText.substring(0, lastIndex - 2)
            } else {
                resultText = resultText.substring(0, lastIndex)
            }
            if (resultText.endsWith('.')) {
                resultText += "0"
            }
        } else {
            Toast.makeText(
                applicationContext,
                "Text is too short to delete a character",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun isDeleteSymbol(element: String): Boolean {
        return "del" == element
    }

    private fun handleOperator(operator: String) {
        if (resultText[resultText.length - 1].toString().matches(Regex("[0-9]"))) {
            resultText += " $operator "
        } else {
            Toast.makeText(applicationContext, "Operation is not allowed here", Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun clearResult() {
        resultText = "0"
    }

    private fun calculateResult() {
        resultText = resultText.replace(Regex("[\n \t]"), "")
        if (!resultText.last().toString().matches(Regex("[0-9]"))) {
            Toast.makeText(
                applicationContext,
                "Input must end with a number",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        calculatePartialResults("*")
        calculatePartialResults("/")
        calculatePartialResults("+")
        calculatePartialResults("-")
    }

    private fun calculatePartialResults(operator: String) {
        while (resultText.contains(operator)) {

            val operatorIndex: Int = if (operator == "-" && resultText.startsWith("-")) {
                if (resultText.indexOf("-") == resultText.lastIndexOf("-")) {
                    break
                }
                resultText.substring(1).indexOf(operator) + 1
            } else {
                resultText.indexOf(operator)
            }

            val firstSubstring = resultText.substring(0, operatorIndex)
            val secondSubstring = resultText.substring(operatorIndex + 1)

            val firstOperandNullable = LAST_NUMBER_REGEX.find(firstSubstring, 0)
            val secondOperandNullable = NUMBER_REGEX.find(secondSubstring, 0)

            var firstOperand = "0"
            var secondOperand = "0"

            if (firstOperandNullable != null) {
                firstOperand = firstOperandNullable.value
            }

            if (secondOperandNullable != null) {
                secondOperand = secondOperandNullable.value
            }

            var doubleResult = 0.0

            when (operator) {
                "*" -> doubleResult = firstOperand.toDouble() * secondOperand.toDouble()
                "/" -> doubleResult = firstOperand.toDouble() / secondOperand.toDouble()
                "+" -> doubleResult = firstOperand.toDouble() + secondOperand.toDouble()
                "-" -> doubleResult = firstOperand.toDouble() - secondOperand.toDouble()
            }

            resultText =
                resultText.replace("$firstOperand$operator$secondOperand", doubleResult.toString())

            if(resultText.contains("NaN")) {
                resultText = "NaN"
                break
            }
        }
    }

    private fun invertResult() {
        if (resultText.contains(Regex("[+/*]"))) {
            Toast.makeText(
                applicationContext,
                "You can only invert numbers, not operations",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (resultText.indexOf('-') == resultText.lastIndexOf('-')) {
            if (resultText.startsWith("-")) {
                resultText = resultText.removeRange(0, 1)
            } else {
                resultText = "-$resultText"
            }
        } else {
            Toast.makeText(
                applicationContext,
                "You can only invert numbers, not operations",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun handleDigit(digit: String) {
        if (resultText == "0") {
            resultText = digit
        } else {
            resultText += digit
        }
    }

    private fun handleDot() {
        if (resultText[resultText.length - 1].toString().matches(Regex("[0-9]"))) {
            val lastNumberNullable = LAST_NUMBER_REGEX.find(resultText, 0)
            if (lastNumberNullable != null) {
                val lastNumber = lastNumberNullable.value
                if (!lastNumber.contains('.')) {
                    resultText += '.'
                }
                return
            }
            if (!resultText.contains('.')) {
                resultText += '.'
            }
        } else {
            Toast.makeText(applicationContext, "Dot is not allowed here", Toast.LENGTH_LONG).show()
        }
    }

    private fun isOperator(input: String): Boolean {
        return operators.contains(input)
    }

    private fun isDotSymbol(input: String): Boolean {
        return "." == input
    }

    private fun isDigit(input: String): Boolean {
        return input.matches(Regex("[0-9]"))
    }

    private fun isEqualitySymbol(input: String): Boolean {
        return "=" == input
    }

    private fun isInverseSymbol(input: String): Boolean {
        return "+/-" == input
    }

    private fun isClearSymbol(input: String): Boolean {
        return "C" == input
    }

    private fun setEventListeners() {
        buttonBack.setOnClickListener {
            onBackPressed()
        }
        button_0.setOnClickListener {
            addElementToResultText("0")
        }
        button_1.setOnClickListener {
            addElementToResultText("1")
        }
        button_2.setOnClickListener {
            addElementToResultText("2")
        }
        button_3.setOnClickListener {
            addElementToResultText("3")
        }
        button_4.setOnClickListener {
            addElementToResultText("4")
        }
        button_5.setOnClickListener {
            addElementToResultText("5")
        }
        button_6.setOnClickListener {
            addElementToResultText("6")
        }
        button_7.setOnClickListener {
            addElementToResultText("7")
        }
        button_8.setOnClickListener {
            addElementToResultText("8")
        }
        button_9.setOnClickListener {
            addElementToResultText("9")
        }
        button_dot.setOnClickListener {
            addElementToResultText(".")
        }
        button_C.setOnClickListener {
            addElementToResultText("C")
        }
        button_change.setOnClickListener {
            addElementToResultText("+/-")
        }
        button_del.setOnClickListener {
            addElementToResultText("del")
        }
        button_eq.setOnClickListener {
            addElementToResultText("=")
        }
        button_add.setOnClickListener {
            addElementToResultText("+")
        }
        button_sub.setOnClickListener {
            addElementToResultText("-")
        }
        button_mult.setOnClickListener {
            addElementToResultText("*")
        }
        button_div.setOnClickListener {
            addElementToResultText("/")
        }
    }
}
