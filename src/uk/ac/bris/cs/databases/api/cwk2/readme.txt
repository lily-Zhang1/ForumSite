您的所有实现都必须此包中，或此包的子包。您将需要一个实现API的类，
您可以创建任意数量的其他“helper”类，以便拥有干净且设计良好的代码

注意，对于从服务器类获得的连接对象，将关闭自动提交（auto-commit will be turned off ）。换句话说，您需要负责提交或回滚任何涉及写入数据库的事务。
task 2的可交付成果是一个ZIP文件，其中包含api包(和任何子包)的java文件。确保只包含java源代码，而不是.class文件。
