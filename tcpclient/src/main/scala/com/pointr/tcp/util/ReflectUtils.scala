package com.pointr.tcp.util

import reflect.runtime.universe._

object ReflectUtils {

  import reflect.runtime.universe.TypeTag

  def instantiate[T](className: String)(args: Any*): T = {
    //    debug(s"Instantiating $className ..")
    val o1 = instantiate(Class.forName(className))(args.map(_.asInstanceOf[AnyRef]): _*)
    val o2 = o1.asInstanceOf[T]
    o2
  }

  def instantiate[T: TypeTag](clazz: java.lang.Class[T])(args: AnyRef*): T = {
    val constructor = clazz.getConstructors().find(_.getParameterTypes.length == args.length).head
    constructor.newInstance(args: _*).asInstanceOf[T]
  }

  def instantiateSimFn[T: TypeTag](clazz: java.lang.Class[T])(args: AnyRef*): T = {
    val constructor = clazz.getConstructors().find(_.getParameterTypes.length == args.length).head
    constructor.newInstance(args: _*).asInstanceOf[T]
  }

  def caseClassMembers[T: TypeTag](t: T) = {
    def getMethods[T: TypeTag] = typeOf[T].members.sorted.collect {
      case m: MethodSymbol if m.isCaseAccessor => m.fullName.substring(m.fullName.lastIndexOf(".") + 1)
    }
    getMethods[T]
  }
}
