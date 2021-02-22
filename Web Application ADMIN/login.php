<?php
require_once 'FireStore.php';
$fs = new FireStore('users');
$name = $_POST['email'];
$pwd = $_POST['pswd'];
if($name == 'admin@abc.com' and $pwd =='admin'){

}else{
    header('Location: test.html');
}
?>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="utf-8" />
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" />
    <meta content='width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=0, shrink-to-fit=no' name='viewport' />
    <link href="assets/css/material-kit.min.css?v=2.2.0" rel="stylesheet" />
    <link rel="stylesheet" type="text/css" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700|Roboto+Slab:400,700|Material+Icons" />
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/latest/css/font-awesome.min.css">
    <title>Admin</title>
</head>

<style>
    body{
    }
    .student{-webkit-text-fill-color: #F7C331;
        -webkit-text-stroke-width: 0.5px;
        -webkit-text-stroke-color: white;
        font-family: Lobster;
    }
    .form-control::-webkit-input-placeholder { color: white; }  /* WebKit, Blink, Edge */
    .form-control:-moz-placeholder { color: white; }  /* Mozilla Firefox 4 to 18 */
    .form-control::-moz-placeholder { color: white; }  /* Mozilla Firefox 19+ */
    .form-control:-ms-input-placeholder { color: white; }  /* Internet Explorer 10-11 */
    .form-control::-ms-input-placeholder { color: white; }  /* Microsoft Edge */
    ::-webkit-scrollbar {
        width: 8px;
    }
    ::-webkit-scrollbar-track {
        background: #f1f1f1;
    }

    /* Handle */
    ::-webkit-scrollbar-thumb {
        background: #888;
    }

    /* Handle on hover */
    ::-webkit-scrollbar-thumb:hover {
        background: #555;
    }
    .background-image {
        position: fixed;
        left: 0;
        right: 0;
        z-index: 1;

        display: block;
        width: 1200px;
        height: 800px;

        -webkit-filter: blur(5px);
        -moz-filter: blur(5px);
        -o-filter: blur(5px);
        -ms-filter: blur(5px);
        filter: blur(5px);
    }
    .weatherwidget-io{
        cursor: not-allowed;
        pointer-events: none;
        width: 350px;
        min-width: 350px;
        border-radius: 10px 0px 10px 0px;
        box-shadow: 0 10px 20px rgba(0,0,0,0.19), 0 6px 6px rgba(0,0,0,0.23);

    }
    .feedgrabbr_widget{
        min-width: 300px;
        padding-bottom: 0px;
        box-shadow: 0 10px 20px rgba(0,0,0,0.19), 0 6px 6px rgba(0,0,0,0.23);
    }
    .info{
        padding-top: 15px;
        padding-bottom: 0px;

    }
    .card{
        min-width: 440px;
    }
    .ca{

        border-radius: 51px 51px;
        width: 70px;
        transition: all 0.3s cubic-bezier(.25,.8,.25,1);


    }
    .ca:hover {
        box-shadow: 0 14px 28px rgba(0,0,0,0.25), 0 10px 10px rgba(0,0,0,0.22);
    }
    .time{
        font-size:25px;
    }
    .date{

        font-size: 15px;

    }
    .timendate{
        display: block;
        background-color: white;
        color: black;
        padding: 5px 10px;

    }
    .lis{
        width: 350px;
        margin-left: 120px;
    }
    .righ{
        font-size: 60px;
    }
    .noticeif,.contentif{
        width: auto;
        height: 350px;
        border: none;
    }

</style>

<body>
<div style="top: 25%;z-index: 999; color: deeppink;text-align: center;
    position: relative;align-items: center;
  justify-content: center;
  text-align: center;" class="name"><h1>Swastha ❤</h1></div>
<div id="backg" class="alert">



    <ul class="nav nav-pills nav-pills-icons info nav-pills-info" role="tablist">

        <li class="nav-item">
            <a class="nav-link active" href="#dashboard-1" role="tab" data-toggle="tab">
                <i class="material-icons">supervisor_account</i>
                Vitals Dashboard
            </a>
        </li>
        <li class="nav-item">
            <a class="nav-link" href="#schedule-1" role="tab" data-toggle="tab">
                <i class="material-icons">description</i>
                Files
            </a>
        </li>

    </ul>

    <div class="tab-content tab-space">


        <div class="tab-pane active" id="dashboard-1">
            <div class="row">
                <div class="container">
                <table class="table">
                    <thead>
                    <tr>
                        <th>User-ID</th>
                        <th>Device</th>
                        <th class="text-right">Cholestrol</th>
                        <th class="text-right">BMI</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td >Andrew Mike</td>
                        <td >2013</td>
                        <td  class="text-right">99,225</td>
                        <td  class="text-right">
                            25
                        </td>
                    </tr>
                        <?php
                        error_reporting(null);
                        print_r($fs->getCollection('users'));
                        ?>



                    </tbody>
                </table>
                </div>
            </div>


        </div>

        <div class="tab-pane" id="schedule-1">
            <div class="container">
                <table class="table">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th class="text-right">Actions</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td >Andrew Mike</td>
                        <td  class="td-actions text-right">
                            <button type="button" rel="tooltip" class="btn btn-success btn-round">
                                <i class="material-icons">get_app</i>
                            </button>
                        </td>
                    </tr>
                    <?php
                    print_r($fs->getCollection2('users'));
                    ?>


                    </tbody>
                </table>
            </div>

        </div>


    </div>
</div>

</body>



<script src="assets/js/core/jquery.min.js" type="text/javascript"></script>
<script src="assets/js/core/popper.min.js" type="text/javascript"></script>
<script src="assets/js/core/bootstrap-material-design.min.js" type="text/javascript"></script>
<script src="assets/js/plugins/moment.min.js"></script>
<script src="assets/js/plugins/nouislider.min.js" type="text/javascript"></script>
<script src="assets/js/plugins/jquery.sharrre.js" type="text/javascript"></script>
<script src="assets/js/material-kit.js?v=2.0.4" type="text/javascript"></script>

</html>

