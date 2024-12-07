# Recursion testcase scraping tool  

学習プラットフォームRecursionで出題される問題の入出力データ(テストケース)をWebスクレイピングして取得するツールです。  

CLIからURLを引数で指定して取得し、JSONファイルに保存します。  
また、引数で複数のURLを書いたテキストファイルを指定し、読み込ませて取得することも可能です。  

## 前提  

Recursionサイト内のデータを取得するツールであるため、有料会員である必要があります。  

また、本ツールを用いて収集したデータを他人に配布することを禁止します。  
データが必要な人が個別に取得して下さい。  

なお、個人的な利用目的でのスクレイピングによるデータ取得については、Recursionから許可をいただいています。  
Recursionに迷惑をかけない範囲で、適切に利用してください。  

### なぜ作ったのか？  

Recursionの問題を解く時の、  

- 自分の慣れ親しんだエディタで問題を解きたい  
- Recursionのサイトでサポートしていないプログラミング言語を使いたい  

といったニーズに向けて開発しました(ﾀﾀﾞｼﾞﾌﾞﾝｶﾞﾔﾘﾀｶｯﾀﾀﾞｹｰ)。  

ローカルに問題の入出力を取得しておけば加工は自由です！  
JSON形式で作成されるため、各種言語のJSONライブラリから読み込みが可能です。  

あとは読み込んだ値を使用して、実行し、アウトプットとの整合をするだけです。  

テストケースの読み込みと実行については、独自にテストライブラリやテスト実行用の関数などを書く必要があります。  
言語全てについて網羅するのは無理ですが、Clojureの例を挙げておきます。  
ローカルで実行したい人は、作るときの参考にしてください。  

[Clojureを使ってローカル環境でテストする方法](/doc/Clojure.md)  

#### お願い

みんなもお気に入りの言語での使い方のドキュメント書いて♥️  
もっとローカル実行環境整備の輪を広げよう！  
各言語でのやり方をまとめたドキュメントのプルリクエスト待ってます！(もし「追加していいよ」という人がいましたら、docディレクトリにmdファイルなどで追加してください)  

---  

## 事前準備  

以下の準備が必要です。  

- Chrome WebDriverのインストール  
- `.env`へのユーザー情報の入力  

### WebDriverのインストール  

スクレイピング用に、ChromeWebDriverを使用してます。  
ChromeWebDriverをインストールしてください。  

インストール方法は環境ごとに多岐にわたります。  
参考として、筆者環境であるMacの場合のみ記載しておきます。  
他の環境については各自で調べてインストールしてください。  

Macのインストール例(Homebrew使用)  
```sh  
brew install chromedriver  
```  

なお、本ツールはChromeWebDriverの以下のバージョンで確認を行っています。  
その他のバージョンについては動作未確認となっています(たぶん動くとは思いますが)。  

```sh  
ChromeDriver 131.0.6778.85  
```  

### .envへの環境情報の設定  

`.env`ファイルを用意し、Recursionのユーザー情報を記載してください。  
ログイン時の情報として使用するため、必須の情報となります。  

```ini  
USER_EMAIL=your_email  
USER_PASSWORD=your_password  
```  

また、ファイルはデフォルトで`jar`と同じディレクトリに、`testcase.json`という名前で作成されます。  
もし、特定のディレクトリに作成したい場合は、`.env`に以下のキーを記述してください。  

```ini  
# 何も設定しない場合はデフォルトのファイルパスに出力します  
OUTPUT_FILEPATH=  
# カレントディレクトリにtestcase.jsonという名前で出力します  
# "./testcase.json"  

# ファイルまで指定するとそのファイル名で出力します  
OUTPUT_FILEPATH="./a/b/c/filename.json"  
# "./a/b/c/filename.json"  

# ファイル名を指定しなければ、デフォルトの名前でセットされます  
OUTPUT_FILEPATH="./a/b/c/"  
# "./a/b/c/testcase.json"  

# なお、ダブルクォーテーション(")で囲まなくても認識します  
OUTPUT_FILEPATH=./a/b/c/filename.json  
# "./a/b/c/filename.json"  
```  

### JVMのインストール  

本ツールは`jar`ファイルとなっているため、あらかじめJVMのインストールを行ってください。  

### jarファイルのダウンロード  

以下のURLから`jar`ファイルをダウンロードして下さい。  

[Release page](https://github.com/kip2/Recursion-tools/releases/tag/v1.0.0)  

URLに移動し、画像に示す場所をクリックすれば、`jar`ファイルがダウンロードされます。  

![jar-download](/pic/jar-download.png)

---  

## Usage  

### 使うための事前準備  

事前準備で作成した`.env`ファイルを、ダウンロードした`jar`と同じディレクトリに配置して下さい。  
`jar`と違うディレクトリから`jar`を実行する場合は、カレントディレクトリに配置してください。  
実行時のユーザーのカレントディレクトリの`.env`ファイルを読み込みます。  

### URLによる実行  

`jar`ファイルのため、以下のように実行してください。  

```sh  
java -jar Recursion-scraping.jar "https://recursionist.io/dashboard/problems/1"  
```  

複数のURL指定も可能です。  

```sh  
java -jar Recursion-scraping.jar "https://recursionist.io/dashboard/problems/1" "https://recursionist.io/dashboard/problems/2"  
```  

### fileからの読み込みによる実行  

複数のURLを引数に渡すことで、複数のURLからデータを取得できます。  
2，3個くらいのURLであれば、引数から渡してください。  

しかし、URLが数十指定するとなると、面倒です。  
その面倒を避けるため、ファイルからの読み込みにも対応しています。  

まず、ファイルに取得対象のURLを並べて記載してください。  
ファイルの注意事項としては以下になります。  

- ファイルはUTF-8のみ対応してます。  
- 一行にURLをひとつだけ、記載してください。  

なお、ファイル名は自由で構いません。お好き名前をつけてください。  

以下はファイルの例です。  

`scraping.txt`  
```  
https://recursionist.io/dashboard/problems/1  
https://recursionist.io/dashboard/problems/2  
https://recursionist.io/dashboard/problems/3  
https://recursionist.io/dashboard/problems/4  
https://recursionist.io/dashboard/problems/5  
```  

準備ができたら、以下のように実行してください。  
`-f`オプションのあとに、インプットファイルのファイルパスを指定してください。  

```sh  
# カレントディレクトリにinput-file.txtがある場合。  
java -jar Recursion-scraping.jar -f input-file.txt  

# input-file.txtが配置されたパスを指定すれば読み込みます。  
java -jar Recursion-scraping.jar -f ./a/b/c/input-file.txt  
```  

以下は補足。  
hLinux系のOSであれば`xargs`コマンドを使用すれば、パイプ処理で渡すことも可能です。  

```sh  
cat input-file.txt | xargs java -jar Recursion-scraping.jar   
```  

### headlessモードなしでの実行  

デフォルトではChrome WebDriverのヘッドレスモードが有効になっています。  
しかし、ヘッドレスモードだと、いつスクレイピングが終わったか分かりづらいなどの問題があるため、ヘッドレスモードを無効にする起動オプションも用意しています。  

以下のように、`-d`オプションをつけて起動して下さい。  

```sh  
# -dオプションを付けることでヘッドレスモードを無効にできます  
java -jar Recursion-scraping.jar "https://recursionist.io/dashboard/problems/1" -d  

# 順番は逆でも指定可能です  
java -jar Recursion-scraping.jar -d "https://recursionist.io/dashboard/problems/1"  

# ファイルからの読み込みでも指定可能です  
java -jar Recursion-scraping.jar -d -f input-file.txt  
```  

### help  

使い方に困ったらヘルプコマンドを実行してください。  
使い方が表示されます。  

```sh  
java -jar Recursion-scraping.jar -h  
```  

以下は出力の例です。  
```sh  
$ java -jar target/Recursion-scraping.jar -h  
  -h, --help         Show help.  
  -d, --disabled-headless  Disabled headless mode.  
  -f, --file FILE      Path to the input file.  
=== 使いかた ===  
java -jar Recursion-scraping.jar https://recursionist.io/dashboard/problems/1  
=== ファイルパス指定の場合(UTF-8のファイルのみ対応) ===  
java -jar Recursion-scraping.jar input-file.txt  
```  

## 免責事項  

本ツールを利用したことにより発生した、利用者の損害及び利用者が第三者に与えた損害について、その損害が直接的又は間接的かを問わず、一切の責任を負いません。  

本ツールを用いて取得したデータを、他人に譲渡したりしないでください。  
取得したデータは、個人利用の範囲、かつ、ローカル環境のみでの利用に限ります。  

また、スクレイピングはRecursionのサーバーに負荷を与える可能性があります。  
連続で使用したりせず、サーバーに負担のないよう、適切な間隔をあけて使用してください。  

## License  

Copyright © 2024 kip2  

This program and the accompanying materials are made available under the  
terms of the Eclipse Public License 2.0 which is available at  
http://www.eclipse.org/legal/epl-2.0.  

This Source Code may also be made available under the following Secondary  
Licenses when the conditions for such availability set forth in the Eclipse  
Public License, v. 2.0 are satisfied: GNU General Public License as published by  
the Free Software Foundation, either version 2 of the License, or (at your  
option) any later version, with the GNU Classpath Exception which is available  
at https://www.gnu.org/software/classpath/license.html.  
