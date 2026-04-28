package br.edu.ufape.agrofeira.config

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.stereotype.Component

@Component
class ApplicationContextProvider : ApplicationContextAware {
    @Throws(BeansException::class)
    override fun setApplicationContext(ctx: ApplicationContext) {
        context = ctx
    }

    companion object {
        lateinit var context: ApplicationContext

        inline fun <reified T : Any> getBean(): T = context.getBean(T::class.java)
    }
}
