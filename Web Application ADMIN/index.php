<?php
require_once 'FireStore.php';
$fs = new FireStore('users');
print_r($fs->getCollection('users'));
//print_r($fs->getDocument('Ny3aznBLVaZMH1lOqQqoksBnvK42'))

?>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>My Records</title>
    <link href="assets/css/material-kit.min.css?v=2.2.0" rel="stylesheet" />
    <link rel="stylesheet" type="text/css" href="https://fonts.googleapis.com/css?family=Roboto:300,400,500,700|Roboto+Slab:400,700|Material+Icons" />
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/font-awesome/latest/css/font-awesome.min.css">
    <script src="buffer.js"></script>
    <script src="index_ipfs.js"></script>

    <script src="sweetalert2.all.min.js"></script>
</head>
<style>
    .table td, .table th{
        vertical-align: middle !important;
    }
    .float{
        position:fixed;
        width:60px;
        height:60px;
        bottom:40px;
        right:40px;
        background-color:#0C9;
        color:#FFF;
        border-radius:50px;
        text-align:center;
        box-shadow: 2px 2px 3px #999;
    }

    .my-float{
        margin-top:22px;
    }
</style>
<body>
</body>
<script src="assets/js/core/jquery.min.js" type="text/javascript"></script>
<script src="assets/js/core/popper.min.js" type="text/javascript"></script>
<script src="assets/js/core/bootstrap-material-design.min.js" type="text/javascript"></script>
<script src="assets/js/plugins/moment.min.js"></script>
<script src="assets/js/material-kit.js?v=2.2.0" type="text/javascript"></script>
</html>
