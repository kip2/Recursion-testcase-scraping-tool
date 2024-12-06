# scraping

Recursionの問題の入出力データを、Webスクレイピングして取得するツールです。

## Prepare

`.env`ファイルを用意し、Recursionのユーザー情報を記載してください。

```env
USER_EMAIL=your_email
USER_PASSWORD=your_password
```

また、ファイルはデフォルトで、jarと同じディレクトリに`testcase.json`という名前で作成されます。
もし、特定のディレクトリに作成したい場合は、`.env`に以下のキーを記述してください。

```env
# 何も設定しない場合はデフォルトのファイルパスに出力します
OUTPUT_FILEPATH=
# カレントディレクトリにtestcase.jsonという名前で出力します
# "./testcase.json"

# ファイルまで指定するとそのファイル名で出力します
OUTPUT_FILEPATH="./a/b/c/filename.json"

# ファイル名を指定しなければ、デフォルトの名前でセットされます
OUTPUT_FILEPATH="./a/b/c/"
# "./a/b/c/testcase.json"

# なお、"で囲まなくても大丈夫です
OUTPUT_FILEPATH=./a/b/c/filename.json
```

## Usage

FIXME

## 免責事項

本ツールを利用したことにより発生した、利用者の損害及び利用者が第三者に与えた損害について、その損害が直接的又は間接的かを問わず、一切の責任を負いません。
本ツールを用いて取得したデータを、他人に譲渡したりしないでください。
あくまで個人的利用の範囲で利用してください。

また、スクレイピングはRecursionサーバーに負荷を与える可能性があります。
連続で使用したりせず、サーバーに負担のないよう、適切な間隔をあけて利用してください。

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
