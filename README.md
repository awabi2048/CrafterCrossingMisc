# Paper Kotlin Plugin Template (1.21.8 向け)

このプロジェクトは、Paper 1.21.8 (API 1.21.4) 向けの Kotlin プラグイン雛形です。

## 特徴
- **Kotlin**: 最新の Kotlin 1.9.22 を使用。
- **Java 21**: Paper 1.21.x に必要な Java 21 をターゲットにしています。
- **Maven**: 依存関係管理に Maven を使用。
- **paper-plugin.yml**: 最新の Paper プラグイン形式。

## ビルド方法
以下のコマンドを実行して JAR ファイルを生成します。

```bash
mvn clean package
```

生成された JAR ファイルは `target/` ディレクトリ配下に作成されます。

## 構成
- `src/main/kotlin`: Kotlin ソースコード
- `src/main/resources`: `paper-plugin.yml` や `config.yml` などのリソースファイル
- `pom.xml`: Maven 設定ファイル
