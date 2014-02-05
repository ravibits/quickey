quickey
=======

GWT Library for handling keyboard events. Will be useful for creating custom shortcuts ala GMail style.

#Maven Dependency Information
To use this as a maven dependency, use the following dependency and repository details.

##dependency details
```
<dependency>
	<groupId>io.github.ravibits</groupId>
	<artifactId>quickey</artifactId>
	<version>1.0.0</version>
</dependency>
```

##repository details
```
<repositories>
    <repository>
        <id>quickey-mvn-repo</id>
        <url>https://raw.github.com/ravibits/quickey/mvn-repo/</url>
        <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
        </snapshots>
    </repository>
</repositories>
```

##Usage
1. First step is to create an instance of Quickey
2. Bind a sequence and a callback to execute for that sequence.
3. Pass in options as third param (optional)
```
Quickey.getInstance().bind("/",new Command(){
      @Override
      public void execute() {
        // do something.
      }
});
```

##TODO
Add full list of options and complete documentation
