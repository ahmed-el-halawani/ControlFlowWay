class ControlFlow() {

    var logger: (String) -> Unit = { }

    constructor(logger: (String) -> Unit) : this() {
        this.logger = logger
    }

    fun flowBuilder(): FlowStep<Unit> {
        return FlowStep {}
    }

    inner class FlowStep<T>(val nextActionA: () -> T) {

        fun <R> then(nextAction: (T?) -> R): FlowStep<R> {
            return then(null, nextAction)
        }

        fun <R> then(stepTitle: String?, nextAction: (T?) -> R): FlowStep<R> {
            return FlowStep {
                val nextActionOut = nextAction(nextActionA());
                stepTitle?.let { logger("$it ,data: ${nextActionOut?.toString() ?: "null"}") }
                nextActionOut
            }
        }

        fun build() {
            nextActionA()
        }

        fun <R> whenCondition(nextDefaultAction: (T?) -> R?): ConditionStep<T, R> {
            return whenCondition(null, nextDefaultAction)
        }

        fun <R> whenCondition(startConditionTitle: String?, nextDefaultAction: (T?) -> R?): ConditionStep<T, R> {
            return ConditionStep(startConditionTitle, nextDefaultAction) { nextActionA() }
        }


        inner class ConditionStep<T, R>(
            private val startConditionTitle: String?,
            val nextDefaultAction: (T?) -> R?,
            val data: () -> T?
        ) {

            private val flowSteps: ArrayList<(T?) -> R?> = ArrayList();

            fun case(condition: (T?) -> Boolean, nextAction: (T?) -> R): ConditionStep<T, R> {
                return case(null, condition, nextAction);
            }

            fun case(caseTitle: String?, condition: (T?) -> Boolean, nextAction: (T?) -> R): ConditionStep<T, R> {
                flowSteps.add { mData ->
                    val nextActionOut = nextAction(mData);
                    if (condition(mData)) {
                        caseTitle?.let { logger("$it, data: $mData") }
                        nextActionOut
                    } else
                        null
                };

                return this

            }

            fun endCondition(): FlowStep<R?> {
                return endCondition(null)
            }

            fun endCondition(endTitle: String?): FlowStep<R?> {
                return FlowStep {
                    val data = data();
                    startConditionTitle?.let(logger)
                    val res = flowSteps.map { it(data) }.firstOrNull { it != null } ?: nextDefaultAction(data)
                    endTitle?.let { logger("$it, data: $res") }
                    res
                };
            }

        }

    }

}
