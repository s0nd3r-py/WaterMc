# Проверка финального дистрибутива

На Windows/JDK 21: python benchmarks/probes/verify-packaged-core.py --jar watermc-server/build/libs/watermc-1.21.11.jar --output D:/water-mc/tmp/watermc-packaged-core-20261112 --java-home "C:/Program Files/Eclipse Adoptium/jdk-21.0.12.101-hotspot"

Запускать после Gradle packaging, последовательно с остальными сборками/серверами и BelowNormal. Скрипт извлекает libraries/server в отдельный output, компилирует probe через @argfile и выполняет его с 1 GiB heap. Ничего не удаляет. Проверяет 16 ключевых классов через ASM canonical serialization, нормализуя CP layout и declaring-owner для inherited field refs, без исключения инструкций/атрибутов. Затем выполняет настоящий Bootstrap/lazy DFU и historical options/item conversion, ZSTD/ZLIB round-trip и наличие JCTools. summary.json/probe.log — доказательства конкретного SHA, а не сравнение производительности.
