import io.reactivex.subjects.PublishSubject
import java.util.LinkedList
import java.util.Observable
import java.util.Queue

class GraphControlFlow<T>(val prop: T, var logger: (String) -> Unit) {
    constructor(prop: T) : this(prop, {}) {
        this.logger = logger
    }

    var firstStep: Stepper = object : Stepper() {
        override fun doStep() {}
    }

    val controller: Controller = Controller()

    fun flowBuilder(): StepperMaker {
        return StepperMaker()
    }


    inner class Controller() {


        fun next() {
//            stepsQueue.poll()?.doStep()
            if (firstStep.nextStep != null) {
                firstStep = firstStep.nextStep!!
                firstStep.doStep()
            }
        }

        fun terminate() {
            terminateCallback?.doStep()
        }

        fun error(throwable: Throwable) {
            errorStepCallback?.doStep()
        }
    }

    inner class StepperMaker {

        var currentStep = firstStep

        fun addStep(stepper: Stepper) {
            currentStep.nextStep = stepper
            currentStep = stepper
        }

        fun setOnTerminate(stepTitle: String?, nextAction: (T?) -> Unit): StepperMaker {
            terminateCallback = FlowStep(stepTitle, nextAction)
            return this;
        }

        fun setOnError(stepTitle: String?, nextAction: (T?) -> Unit): StepperMaker {
            errorStepCallback = FlowStep(stepTitle, nextAction)
            return this;
        }


        fun whenCondition(): ConditionStep {
            return whenCondition(null) {}
        }

        fun whenCondition(nextDefaultAction: (StepperMaker) -> Unit): ConditionStep {
            return whenCondition(null, nextDefaultAction);
        }

        fun whenCondition(startConditionTitle: String?, nextDefaultAction: (StepperMaker) -> Unit): ConditionStep {
            val condition = ConditionStep(this, startConditionTitle, nextDefaultAction)
            addStep(condition)
            return condition;
        }

        fun whenConditionAndNext(
            startConditionTitle: String?,
            nextDefaultAction: (StepperMaker) -> Unit
        ): ConditionStep {
            val condition = ConditionStep(this, startConditionTitle, nextDefaultAction)
            addStep(condition)
            return condition;
        }

        fun then(nextAction: (T?) -> Unit): StepperMaker {
            return then(null, nextAction)
        }

        fun then(stepTitle: String?, nextAction: (T?) -> Unit): StepperMaker {
            addStep(FlowStep(stepTitle) {
                nextAction(prop)
            })
            return this;
        }

        fun thenAndNext(stepTitle: String?, nextAction: (T?) -> Unit): StepperMaker {
            addStep(FlowStep(stepTitle, isAndNext = true) {
                nextAction(prop)
            })
            return this;
        }

        fun build() = controller
    }

    inner class FlowStep(private val description: String?, val isAndNext: Boolean = false, val action: (T?) -> Unit) :
        Stepper() {

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
        var nextDefaultAction: (StepperMaker) -> Unit = {}
    ) :
        Stepper() {
        private val flowSteps: ArrayList<(StepperMaker) -> Unit?> = ArrayList();

        fun case(caseTitle: String?, condition: (T?) -> Boolean, body: (StepperMaker) -> Unit): ConditionStep {
            flowSteps.add {
                if (condition(prop)) {
                    caseTitle?.let { logger("$it, data: $prop") }
                    body.invoke(it)
                } else
                    null
            }

            return this
        }

        fun defaultCase(caseTitle: String?, body: (StepperMaker) -> Unit): ConditionStep {
            nextDefaultAction = {
                caseTitle?.let { logger("$it, data: $prop") }
                body.invoke(it)
            }
            return this
        }

        fun build(): Controller {
            return controller;
        }

        override fun doStep() {
            startConditionTitle?.let(logger)
            var isOutWithoutValue = true;
            for (it in flowSteps) {
                if (it(stepperMaker) != null) {
                    isOutWithoutValue = false;
                    break
                }
            }

            if (isOutWithoutValue)
                nextDefaultAction(stepperMaker)

            controller.next()
        }

    }

    abstract inner class Stepper {
        var nextStep: Stepper? = null

        abstract fun doStep()
    }

    private val stepsQueue: Queue<Stepper> = LinkedList()
    private var errorStepCallback: Stepper? = null;
    private var terminateCallback: Stepper? = null;
}
