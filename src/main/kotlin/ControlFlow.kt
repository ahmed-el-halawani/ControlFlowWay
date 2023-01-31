import java.util.LinkedList
import java.util.Queue

class ControlFlow<T>(val prop: T, var logger: (String) -> Unit) {
    constructor(prop: T) : this(prop, {}) {
        this.logger = logger
    }

    val controller: Controller = Controller()

    fun flowBuilder(): StepperMaker {
        return StepperMaker()
    }


    inner class Controller() {
        fun next() {
            stepsQueue.poll()?.doStep()
        }

        fun terminate() {
            terminateCallback?.doStep()
        }

        fun error(throwable: Throwable) {
            errorStepCallback?.doStep()
        }
    }

    inner class StepperMaker {

        fun setOnTerminate(stepTitle: String?, nextAction: (T?) -> Unit): StepperMaker {
            terminateCallback = FlowStep(stepTitle, nextAction)
            return this;
        }

        fun setOnError(stepTitle: String?, nextAction: (T?) -> Unit): StepperMaker {
            errorStepCallback = FlowStep(stepTitle, nextAction)
            return this;
        }

        fun whenCondition(nextDefaultAction: (T?) -> Unit): ConditionStep {
            return whenCondition(null, nextDefaultAction);
        }

        fun whenCondition(startConditionTitle: String?, nextDefaultAction: (T?) -> Unit): ConditionStep {
            val condition = ConditionStep(this, startConditionTitle, nextDefaultAction)
            stepsQueue.add(condition)
            return condition;
        }

        fun whenConditionAndNext(startConditionTitle: String?, nextDefaultAction: (T?) -> Unit): ConditionStep {
            val condition = ConditionStep(this, startConditionTitle, nextDefaultAction)
            stepsQueue.add(condition)
            return condition;
        }

        fun then(nextAction: (T?) -> Unit): StepperMaker {
            return then(null, nextAction)
        }

        fun then(stepTitle: String?, nextAction: (T?) -> Unit): StepperMaker {
            stepsQueue.add(FlowStep(stepTitle) {
                nextAction(prop)
            })
            return this;
        }

        fun thenAndNext(stepTitle: String?, nextAction: (T?) -> Unit): StepperMaker {
            stepsQueue.add(FlowStep(stepTitle, isAndNext = true) {
                nextAction(prop)
            })
            return this;
        }

        fun build() = controller
    }

    inner class FlowStep(private val description: String?, val isAndNext: Boolean = false, val action: (T?) -> Unit) :
        Stepper {

        constructor(action: (T?) -> Unit) : this(null, false, action)
        constructor(description: String?, action: (T?) -> Unit) : this(description, false, action)

        override fun doStep() {
            action(prop)
            description?.let { logger("$it ,data: ${prop?.toString() ?: "null"}") }
            if (isAndNext) {
                controller.next()
            }
        }


    }

    open inner class ConditionStep(
        private val stepperMaker: StepperMaker,
        private val startConditionTitle: String?,
        val nextDefaultAction: (T?) -> Unit
    ) :
        Stepper {
        var isAndNext: Boolean = false
        private var endConditionTitle: String? = null

        private val flowSteps: ArrayList<(T?) -> Unit?> = ArrayList();

        fun case(condition: (T?) -> Boolean, nextAction: (T?) -> Unit): ConditionStep {
            return case(null, condition, nextAction);
        }

        fun case(caseTitle: String?, condition: (T?) -> Boolean, nextAction: (T?) -> Unit): ConditionStep {
            flowSteps.add { mData ->
                val nextActionOut = nextAction(mData);
                if (condition(mData)) {
                    caseTitle?.let { logger("$it, data: $prop") }
                    nextActionOut
                } else null
            };

            return this

        }

        fun endCondition(): StepperMaker {
            return endCondition(null);
        }

        fun endCondition(endConditionTitle: String?): StepperMaker {
            this.endConditionTitle = endConditionTitle;
            return stepperMaker;
        }

        fun endConditionAndNext(): StepperMaker {
            this.isAndNext = true
            return endCondition()
        }

        fun endConditionAndNext(endConditionTitle: String?): StepperMaker {
            this.isAndNext = true
            return endCondition(endConditionTitle)
        }

        override fun doStep() {
            startConditionTitle?.let(logger)
            flowSteps.map { it(prop) }.firstOrNull { it != null } ?: nextDefaultAction(prop)
            endConditionTitle?.let { logger("$it, data: $prop") }
            if (isAndNext)
                controller.next()
        }

    }


    private interface Stepper {
        fun doStep()
    }

    private val stepsQueue: Queue<Stepper> = LinkedList()
    private var errorStepCallback: Stepper? = null;
    private var terminateCallback: Stepper? = null;
}
