var table = $('#example5').DataTable({
    searching: false,
    paging:true,
    select: false,
    info: true,         
    lengthChange:false ,
    language: {
        paginate: {
          next: '<span></span><i class="fas fa-chevron-right"></i>',
          previous: '<i class="fas fa-chevron-left"></i><span></span>' 
        }
      },	
}); 