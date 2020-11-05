package com.example.calculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_calc_sci.*
import kotlinx.android.synthetic.main.activity_calc_simple.buttonBack
import kotlinx.android.synthetic.main.activity_calc_simple.button_0
import kotlinx.android.synthetic.main.activity_calc_simple.button_1
import kotlinx.android.synthetic.main.activity_calc_simple.button_2
import kotlinx.android.synthetic.main.activity_calc_simple.button_3
import kotlinx.android.synthetic.main.activity_calc_simple.button_4
import kotlinx.android.synthetic.main.activity_calc_simple.button_5
import kotlinx.android.synthetic.main.activity_calc_simple.button_6
import kotlinx.android.synthetic.main.activity_calc_simple.button_7
import kotlinx.android.synthetic.main.activity_calc_simple.button_8
import kotlinx.android.synthetic.main.activity_calc_simple.button_9
import kotlinx.android.synthetic.main.activity_calc_simple.button_C
import kotlinx.android.synthetic.main.activity_calc_simple.button_add
import kotlinx.android.synthetic.main.activity_calc_simple.button_change
import kotlinx.android.synthetic.main.activity_calc_simple.button_del
import kotlinx.android.synthetic.main.activity_calc_simple.button_div
import kotlinx.android.synthetic.main.activity_calc_simple.button_dot
import kotlinx.android.synthetic.main.activity_calc_simple.button_eq
import kotlinx.android.synthetic.main.activity_calc_simple.button_mult
import kotlinx.android.synthetic.main.activity_calc_simple.button_sub
import kotlinx.android.synthetic.main.activity_calc_simple.display
import java.lang.Exception
import kotlin.math.*

class CalcSci : AppCompatActivity() {
    var resultText = "0"
    private val operators = setOf("+", "-", "*", "/", "^")
    private val complexOperators = setOf("sqrt", "ln", "log", "sin", "cos", "tan", "pow2")
    private val NUMBER_REGEX = Regex("-?([0-9]+(\\.[0-9]+)?|Infinity)")
    private val LAST_NUMBER_REGEX = Regex("-?([0-9]+(\\.[0-9]+)?|Infinity)$")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calc_sci)
        setEventListeners()
    }

    private fun addElementToResultText(element: String) {
        if (resultText in listOf("NaN", "Infinity", "-Infinity")) {
            resultText = "0"
        }

        when {
            isOperator(element) -> handleOperator(element)
            isComplexOperator(element) -> handleComplexOperator(element)
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

    private fun handleComplexOperator(operator: String) {
        if (resultText.contains(Regex("[+/*^]"))) {
            Toast.makeText(
                applicationContext,
                "You can only perform this operation on numbers",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        val doubleValue: Double
        try {
            doubleValue = resultText.toDouble()
        } catch (e: Exception) {
            Toast.makeText(
                applicationContext,
                "You can only perform this operation on numbers",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        when (operator) {
            "sin" -> resultText = sin(Math.toRadians(doubleValue)).toString()
            "cos" -> resultText = cos(Math.toRadians(doubleValue)).toString()
            "tan" -> resultText = tan(Math.toRadians(doubleValue)).toString()
            "sqrt" -> resultText = sqrt(doubleValue).toString()
            "ln" -> resultText = ln(doubleValue).toString()
            "log" -> resultText = log10(doubleValue).toString()
            "pow2" -> resultText = doubleValue.pow(2.0).toString()
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

        calculatePartialResults("^")
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
                "^" -> doubleResult = firstOperand.toDouble().pow(secondOperand.toDouble())
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
        if (resultText.contains(Regex("[+/*^]"))) {
            Toast.makeText(
                applicationContext,
                "You can only invert numbers, not operations",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (resultText.indexOf('-') == resultText.lastIndexOf('-')) {
            resultText = if (resultText.startsWith("-")) {
                resultText.removeRange(0, 1)
            } else {
                "-$resultText"
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

    private fun isComplexOperator(input: String): Boolean {
        return complexOperators.contains(input)
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
        button_sin.setOnClickListener {
            addElementToResultText("sin")
        }
        button_cos.setOnClickListener {
            addElementToResultText("cos")
        }
        button_tan.setOnClickListener {
            addElementToResultText("tan")
        }
        button_sqrt.setOnClickListener {
            addElementToResultText("sqrt")
        }
        button_ln.setOnClickListener {
            addElementToResultText("ln")
        }
        button_log.setOnClickListener {
            addElementToResultText("log")
        }
        button_pow2.setOnClickListener {
            addElementToResultText("pow2")
        }
        button_powY.setOnClickListener {
            addElementToResultText("^")
        }
    }
}
