package ru.dpav.weather.strategies

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.ViewCommand
import com.arellomobile.mvp.viewstate.strategy.StateStrategy

class AddToEndSingleByTagStateStrategy : StateStrategy {
	override fun <View : MvpView?> beforeApply(currentState: MutableList<ViewCommand<View>>,
		incomingCommand: ViewCommand<View>) {
		val iterator: Iterator<ViewCommand<View>> = currentState.iterator()
		while (iterator.hasNext()) {
			val entry: ViewCommand<View> = iterator.next()
			if (entry.tag == incomingCommand.tag) {
				currentState.remove(entry)
				break
			}
		}
		incomingCommand.let { currentState.add(it) }
	}

	override fun <View : MvpView?> afterApply(currentState: MutableList<ViewCommand<View>>?,
		incomingCommand: ViewCommand<View>?) {
	}
}