* `Arrays.sort(persons, (l, r) -> l.getAge() - r.getAge());`
* ` Integer.valueOf(o1.getAge()).compareTo(
Integer.valueOf(o2.getAge()))`
* `((Integer)o1.getAge()).compareTo((Integer)o2.getAge())`
* Не переносите уже имеющийся код в другие места, в том числе этого же класса.
* 	Не изменяйте отступы, даже, если вы любите KR.
* 	Не уже используемые импорты при реализации-изменении бизнес-логики
* 	`Person person = FluentIterable.from(persons)
.filter(p -> p.getAge() == 30)
.first()
.get();`

