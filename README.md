# HVC-C SDK and Sample for Android Studio
## HEADS UP!!
本アプリケーションはオムロン社の[ヒューマンビジョンコンポBluetooth LEモデル（HVC-C1B)の公式SDK及びサンプルアプリ](http://plus-sensing.omron.co.jp/product/sdk-download.html)をAndroid Studio用に修正したものになります．

以下は本家`ReadMe_J.txt`より引用．

---

>
> ----------------------------------------------------
>  HVC-C Android-JAVAサンプルコード
> ----------------------------------------------------
>
> (1) サンプルコード内容
>   本サンプルではBluetooth接続から機能実行、切断処理までをJAVAのAPIクラスとして用意しています。
>   HVC-Cで9機能(人体検出、手検出、顔検出、顔向き推定、年齢推定、性別推定、視線推定、目つむり推定、
>   表情推定)のすべてを実行し、その結果をTextViewに出力しています。
>
> (2) ディレクトリ構成
>       AndroidManifest.xml           このデモアプリのマニュフェスト
>       src/
>         omron/
>           HVC/                        HVCクラスパッケージ
>             HVC.java                  HVCの親クラス
>             HVC_BLE.java              HVC-Cクラス(HVCのサブクラス)
>             HVC_VER.java              HVCのバージョン番号を格納するクラス
>             HVC_PRM.java              各種パラメータの設定値を格納するクラス
>             HVC_RES.java              機能実行結果を格納するクラス
>             HVCBleCallback            HVC_BLEからメインアクティビティにデバイスの状態を返すための
>                                       コールバッククラス
>             BleDeviceService.java     Bluetoothデバイス管理クラス
>             BleCallback.java          BleDeviceServiceからHVC_BLEにデバイスの状態を返すための
>                                       コールバッククラス
>             BleDeviceSearch.java      Bluetoothデバイスを検索するクラス
>           SimpleDemo/                 サンプルデモのパッケージ
>             MainActivity.java         メインアクティビティクラス
>       res/
>         layout/
>           main.xml                    メインアクティビティの画面構成を定義
>           devices.xml                 デバイスを選択するダイアログの画面構成を定義
>         values/
>           strings.xml                 メインアクティビティで使用する文字列定義
>
> (3) サンプルコードのビルド方法
>   1. サンプルコードはJAVAのバージョン1.7でコンパイルを確認しています。
>      またAndroid-SDKは4.3以上を使用する必要があります。
>
>  2. Bluetoothパーミッション
>      HVC-CはBluetooth接続であるため、アプリケーションにはBluetoothパーミッションの
>      許可が必要です。アプリケーションマニュフェストに以下の記述を追加してください。
>
>     <uses-permission android:name="android.permission.BLUETOOTH" />
>    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
>
>   3. 機能実行サンプル
>      画面は機能実行ボタンとログ出力用のTextViewのみで構成しています。
>      HVC-Cデバイスの特定にデバイス名を使用しています。87行目の
>          BluetoothDevice device = SelectHVCDevice("OMRON_HVC.*|omron_hvc.*");
>      をご確認ください。
>      HVC-C機能実行部のコードはMainActivity.javaのonPostExecute()コールバックメソッドを
>      参考にしてください。
>
>(4) ソースの移植について
>   1. omron.HVC パッケージについて
>        このディレクトリ以下のコードは汎用性を考えて作成しています。
>        JAVAをコンパイルできる環境であればこのままで使用していただけます。
>
>  2. AndroidManifest.xml
>        マニュフェストの記述はこのファイルを参考にしてください。
>
>   3. BleDeviceSearch.java
>        Bluetoothの初期化・デバイスの検索はこちらのコンストラクタからの一連の
>       処理を参考にしてください。デバイスの検索にはBluetoothAdapterのstartDiscovery()
>        メソッドを使用しています。
>
> ※注意事項：
>      HVC_BLEクラスはnewした後、connect()メソッドで引数に設定されたデバイスと
>     接続処理に入ります。接続完了までにはある程度の時間がかかります。コールバックメソッド
>      onConnected()を使用して接続完了を確認後に機能実行する必要があります。
>      （接続中に機能実行を呼び出すとエラーが返ってきます。）
>
>
>[ご使用にあたって]
> ・本サンプルコードおよびドキュメントの著作権はオムロンに帰属します。
> ・本サンプルコードは動作を保証するものではありません。
>
> ----
>オムロン株式会社
> Copyright(C) 2014 OMRON Corporation, All Rights Reserved.
>
