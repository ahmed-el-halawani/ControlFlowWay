import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class GraphControlFlowJ<T> {


    public GraphControlFlowJ(T prop, Consumer<String> logger) {
        this.prop = prop;
        this.logger = logger;
    }

    public GraphControlFlowJ(T prop) {
        this.prop = prop;
    }


    Controller controller = new Controller();

    private final Queue<Stepper> stepsQueue = new LinkedList<>();
    private Stepper errorStepCallback = null;
    private Stepper terminateCallback = null;
    private Consumer<String> logger;
    private final T prop;

    private Stepper firstStep = new Stepper() {
        @Override
        void doStep() {

        }
    };


    public StepperMaker flowBuilder() {
        return new StepperMaker();
    }


    public class StepperMaker {

        Stepper currentStep = firstStep;


        public void addStep(Stepper stepper) {
            currentStep.nextStep = stepper;
            currentStep = stepper;
        }

        public StepperMaker setOnTerminate(String stepTitle, Consumer<T> nextAction) {
            terminateCallback = new FlowStep(stepTitle, nextAction);
            return this;
        }

        public StepperMaker setOnError(String stepTitle, Consumer<T> nextAction) {
            errorStepCallback = new FlowStep(stepTitle, nextAction);
            return this;
        }


        ConditionStep whenCondition() {
            return whenCondition(null, (s) -> {
            });
        }

        ConditionStep whenCondition(Consumer<StepperMaker> nextDefaultAction) {
            return whenCondition(null, nextDefaultAction);
        }

        ConditionStep whenCondition(
                String startConditionTitle,
                Consumer<StepperMaker> nextDefaultAction
        ) {
            ConditionStep condition = new ConditionStep(this, startConditionTitle, nextDefaultAction);
            addStep(condition);
            return condition;
        }

        ConditionStep whenConditionAndNext(
                String startConditionTitle,
                Consumer<StepperMaker> nextDefaultAction
        ) {
            ConditionStep condition = new ConditionStep(this, startConditionTitle, nextDefaultAction);
            addStep(condition);
            return condition;
        }

        StepperMaker then(Consumer<T> nextAction) {
            return then(null, nextAction);
        }

        StepperMaker then(String stepTitle, Consumer<T> nextAction) {
            addStep(new FlowStep(stepTitle, (it) -> nextAction.accept(prop)));
            return this;
        }

        StepperMaker thenAndNext(String stepTitle, Consumer<T> nextAction) {
            addStep(new FlowStep(stepTitle, true, (it) -> {
                nextAction.accept(prop);
            }));
            return this;
        }

        Controller build() {
            return controller;
        }

        ;
    }

    class FlowStep extends Stepper {
        String description;
        Boolean isAndNext = false;
        Consumer<T> action;

        public FlowStep(Consumer<T> action, boolean isAndNext) {
            this.action = action;
            this.isAndNext = isAndNext;
        }

        public FlowStep(String description, Consumer<T> action) {
            this.action = action;
            this.description = description;
        }

        public FlowStep(String description, Boolean isAndNext, Consumer<T> action) {
            this.description = description;
            this.action = action;
            this.isAndNext = isAndNext;
        }

        @Override
        void doStep() {
            action.accept(prop);
            logger.accept(String.format("%s ,data: %s", description, prop.toString()));
            if (isAndNext)
                controller.next();
        }

    }

    class ConditionStep extends Stepper {
        private StepperMaker stepperMaker;
        private String startConditionTitle;
        Consumer<StepperMaker> nextDefaultAction;

        public ConditionStep(StepperMaker stepperMaker, String startConditionTitle, Consumer<StepperMaker> nextDefaultAction) {
            this.stepperMaker = stepperMaker;
            this.startConditionTitle = startConditionTitle;
            this.nextDefaultAction = nextDefaultAction;
        }

        private ArrayList<Function<StepperMaker, Boolean>> flowSteps = new ArrayList<>();

        ConditionStep conditionCase(String caseTitle, Predicate<T> condition, Consumer<StepperMaker> body) {
            flowSteps.add(
                    stepperMaker -> {
                        if (condition.test(prop)) {
                            logger.accept(String.format("$s, data: $s", caseTitle, prop.toString()));
                            body.accept(stepperMaker);
                            return true;
                        }
                        return null;
                    }
            );

            return this;
        }

        ConditionStep defaultCase(String caseTitle, Consumer<StepperMaker> body) {
            nextDefaultAction =
                    stepperMaker -> {
                        logger.accept(String.format("$s, data: $s", caseTitle, prop.toString()));
                        body.accept(stepperMaker);
                    };

            return this;
        }


        Controller build() {
            return controller;
        }

        @Override
        void doStep() {
            logger.accept(startConditionTitle);
            boolean isOutWithoutValue = true;
            for (Function<StepperMaker, Boolean> it : flowSteps) {
                if (it.apply(stepperMaker) != null) {
                    isOutWithoutValue = false;
                    break;
                }
            }

            if (isOutWithoutValue)
                nextDefaultAction.accept(stepperMaker);

            controller.next();
        }

    }

    private abstract static class Stepper {
        Stepper nextStep = null;

        abstract void doStep();
    }


    class Controller {
        public void next() {
//            stepsQueue.poll()?.doStep()
            if (firstStep.nextStep != null) {
                firstStep = firstStep.nextStep;
                firstStep.doStep();
            }
        }

        void terminate() {
            terminateCallback.doStep();
        }

        void error(Throwable throwable) {
            errorStepCallback.doStep();
        }
    }


}
